import { ReactNode, useCallback, useEffect, useMemo, useRef, useState } from 'react';
import './App.css';
import Proof from './components/proof/ProofViewer';
import Header from './components/Header';
import Menu, { MenuHandle } from './components/menu/Menu';
import NewProofModal from './components/modal/NewProofModal';
import ExerciseList, { ExercisesState } from './components/exercises/ExerciseList';
import { StepDto, ProofDto, Exercise } from './types';
import { DEFAULT_LOGIC, INITIAL_STATE, LOGICS, hasStates, logicInfo } from './constant';
import { fetchExercises, loadProofFromText, replayProof, undoLastStep } from './service/actions';
import { markExerciseSolved, readSolvedExercises } from './service/solvedExercises';
import { isRelationFormula, loadedGoal, loadsBackWithSameGoal, proofToText } from './service/utils';
import { clearSavedProof, readSavedProof, writeSavedProof } from './service/savedProof';

// The steps counted as the proof's premises: a leading run of `Ass` steps at assumption level 0, the shape
// `handleNewProofSubmit` creates. The backend treats exactly this prefix as the premises when it replays a proof
// (see `ClassicalProofTransformer.replayProof` / `ProofParser.extractAssumptions`), so it is what "nothing beyond
// the premises" means for both Undo (nothing left to undo) and New Proof (nothing to lose by discarding it).
function premiseCount(steps: StepDto[]): number {
  let count = 0;
  for (const step of steps) {
    if (step.assmsLevel !== 0 || step.rule !== 'Ass') break;
    count++;
  }
  return count;
}

// The `extraParameters` of a premise. In a logic with states the backend (`ModalProofTransformer.initialAssumption`)
// rejects a premise that is not in the initial state. A relation between states (`s0 <= s1`) is in no state: the
// backend neither checks nor keeps one for it, and answers it with `{}`, so it gets none here either.
function premiseParameters(premise: string, proofLogic: string): Record<string, string> {
  return hasStates(proofLogic) && !isRelationFormula(premise, proofLogic) ? { state: INITIAL_STATE } : {};
}

// The proof "Try an example" starts: p -> q and p prove q in one Modus Ponens step.
const EXAMPLE_PREMISES = ['p -> q', 'p'];
const EXAMPLE_GOAL = 'q';

// What happens to a copied proof text in the New Proof dialog: `POST .../proof` rejects a text whose last line is not
// at the top level, and otherwise takes that last line as the goal. So `done` alone does not promise that the text loads
// back as the same proof (a top-level step other than the last may be the goal, e.g. a premise that already is the
// goal), and an unfinished proof ending at the top level (e.g. only its premises) loads back as a finished proof of its
// last line, not rejected.
function copyLoadNote(proof: ProofDto): ReactNode {
  if (!proof.done) {
    const last = proof.steps.at(-1);
    if (last?.assmsLevel !== 0) return 'It only loads back in the New Proof dialog once the proof is finished.';
    return <>It is not finished, so it loads back in the New Proof dialog as a proof of its last line <code>{loadedGoal(proof)}</code> instead of the goal <code>{proof.goal}</code>.</>;
  }
  if (loadsBackWithSameGoal(proof)) return 'To load it again, paste it in the New Proof dialog.';
  return <>Its last line is not the goal, so it loads back in the New Proof dialog with the goal <code>{loadedGoal(proof)}</code> instead of <code>{proof.goal}</code>.</>;
}

// What "Copy proof as text" did with `proof`: its text, and whether the clipboard took it.
type CopyResult = { proof: ProofDto, text: string, copied: boolean };

// Says what "Copy proof as text" did. When the clipboard is not available, it shows the text to copy by hand.
function CopyStatus({ copy }: { copy: CopyResult }) {
  if (copy.copied) {
    return (
      <output className="copy-status" aria-live="polite">
        Proof copied to the clipboard as text. {copyLoadNote(copy.proof)}
      </output>
    );
  }
  return (
    <div className="copy-status">
      <output aria-live="polite">
        The clipboard is not available here. Copy the proof text below by hand.{' '}
        {!loadsBackWithSameGoal(copy.proof) && copyLoadNote(copy.proof)}
      </output>
      <label htmlFor="copied-proof-text" className="visually-hidden">Proof as text</label>
      <textarea
        id="copied-proof-text"
        className="copy-fallback"
        value={copy.text}
        readOnly
        rows={Math.min(copy.proof.steps.length, 10)}
        wrap="off"
        onFocus={(e) => e.target.select()}
      />
    </div>
  );
}

function App() {
  const [colorMapping, setColorMapping] = useState(new Map<number, string>());
  const [isModalOpen, setIsModalOpen] = useState(false);
  // The control that had the focus when the dialog was opened, read before the page goes inert (a browser moves the
  // focus away from something that becomes inert), so that the dialog can hand the focus back to it.
  const [modalOpener, setModalOpener] = useState<HTMLElement | null>(null);
  // Bumped for every new proof; it is the key of the Menu, so that the selected rule, the typed inputs and the last
  // error of the previous proof do not carry over.
  const [proofId, setProofId] = useState(0);
  const menuRef = useRef<MenuHandle>(null);
  const newProofButtonRef = useRef<HTMLButtonElement>(null);
  // Mirrors `proofId`, readable from inside an in-flight undo request's callback without that closure capturing a
  // stale value: an undo response that comes back after a newer proof was started (New Proof can be clicked while
  // an undo is still pending) must not overwrite that newer proof.
  const proofIdRef = useRef(0);
  // Bumped whenever the user starts getting a new proof ("Try an example", or New Proof opening its dialog or its
  // confirmation), before that proof is shown. `proofIdRef` only changes once a proof is on screen, so the restore of
  // the saved proof reads this one too: its answer must not replace a proof the user already asked for.
  const userStartedRef = useRef(0);
  const [proof, setProof] = useState<ProofDto>({
    steps: [],
    logic: DEFAULT_LOGIC,
    goal: '',
  });
  // The logic of everything on the page: the proof on screen, its rules, the exercises, "Try an example", and where the New
  // Proof dialog starts. Before any proof is shown it is the one picked on the empty page.
  const logic = proof.logic;
  // Mirrors `proof`, for an in-flight exercise start to see whether the proof on screen changed while it waited (a rule
  // applied in the Menu, or an undo, neither of which bumps `proofIdRef`).
  const proofRef = useRef(proof);
  useEffect(() => { proofRef.current = proof; }, [proof]);

  // Shown next to the toolbar's New Proof button instead of starting a new proof right away (opening the dialog, or
  // starting an exercise), whenever the current proof has more than its premises: an in-page confirmation, not
  // `window.confirm`, so it can be tested and styled like the rest of the UI, and so the page behind it never needs to go
  // inert. It holds what "Discard and start new" goes on to do.
  const [pendingDiscard, setPendingDiscard] = useState<(() => void) | null>(null);
  const confirmingNewProof = pendingDiscard !== null;
  const confirmCancelRef = useRef<HTMLButtonElement>(null);
  const [isUndoing, setIsUndoing] = useState(false);
  const [undoError, setUndoError] = useState('');
  // What "Copy proof as text" did, and for which proof: copied to the clipboard, or (when the clipboard cannot be used,
  // e.g. outside a secure context) the text, shown for the user to copy by hand. It is only shown while that proof is
  // the one on screen, since it is out of date as soon as the proof changes.
  const [copyResult, setCopyResult] = useState<CopyResult | null>(null);
  const currentCopy = copyResult?.proof === proof ? copyResult : null;
  // Bumped whenever the New Proof dialog closes, so that a "Load from text" or Start Proof answer arriving after the
  // user cancelled the dialog is dropped instead of replacing the proof on screen.
  const dialogSessionRef = useRef(0);
  // "Try an example": whether its proof is being checked by the backend, and why it could not be started.
  const [isStartingExample, setIsStartingExample] = useState(false);
  const [exampleError, setExampleError] = useState('');
  // Whether the proof saved before a reload is being replayed by the backend, and why it could not be restored.
  const [isRestoring, setIsRestoring] = useState(false);
  const [restoreError, setRestoreError] = useState('');
  // The id of the exercise the proof on screen was started from, or null. It is saved with the proof, so that a proof
  // restored after a reload still counts for its exercise.
  const [exerciseId, setExerciseId] = useState<string | null>(null);
  // Whether the list of exercises is shown, and the exercises themselves: null until they are first needed (the list is
  // opened, or a proof of an exercise is on screen and "Next exercise" needs to know the next one).
  const [isExercisesOpen, setIsExercisesOpen] = useState(false);
  // They are fetched per logic, so the ones of another logic count as not fetched yet.
  const [exercisesOf, setExercisesOf] = useState<{ logic: string, state: ExercisesState } | null>(null);
  const exercisesState = exercisesOf?.logic === logic ? exercisesOf.state : null;
  // The ids of the solved exercises of the logic, kept in localStorage (see `solvedExercises`), together with the ones
  // solved in this page session, per logic, which still count when storage cannot be used.
  const [solvedThisSession, setSolvedThisSession] = useState<Record<string, string[]>>({});
  const solved = useMemo(
    () => new Set([...readSolvedExercises(logic), ...(solvedThisSession[logic] ?? [])]),
    [logic, solvedThisSession]);
  // The exercise being checked by the backend before it is shown, and why the last one could not be started.
  const [startingExerciseId, setStartingExerciseId] = useState<string | null>(null);
  const [exerciseStartError, setExerciseStartError] = useState('');
  const exercisesButtonRef = useRef<HTMLButtonElement>(null);
  // Set when "Browse exercises" opens the list: that button goes away with the list open, so the list's heading takes
  // the focus once it is shown.
  const focusExercisesOnOpenRef = useRef(false);

  const onColorChange = useCallback((color: string, line: number) => {
    setColorMapping(colorMapping => {
        const newColoringMap = new Map<number,string>(colorMapping);
        newColoringMap.forEach((value, key) => {
          if (value === color) {
            newColoringMap.delete(key);
          }
        });
        if (line >= 0) {
          newColoringMap.set(line, color);
        }
        return newColoringMap
    });
  }, []);

  // A click (or Enter, or Space) on a row of the proof picks that line for the rule being filled in the menu.
  const handleSelectLine = useCallback((line: number) => menuRef.current?.selectLine(line), []);

  // Does `proceed` straight away when there is nothing to lose; otherwise asks first (see `pendingDiscard`). `opener` is
  // who gets the focus back when the user cancels, or closes the dialog `proceed` opened.
  const askBeforeDiscarding = (opener: HTMLElement | null, proceed: () => void) => {
    setModalOpener(opener);
    if (proof.steps.length > premiseCount(proof.steps)) {
      setPendingDiscard(() => proceed);
    } else {
      // A confirmation still shown for an earlier request (e.g. an exercise start queued it) is superseded by this one.
      setPendingDiscard(null);
      proceed();
    }
  };

  // Goes to the dialog, from whichever control (the toolbar button, or the Menu's once a proof is done) asked for a new
  // proof.
  const requestNewProof = (opener: HTMLElement | null) => {
    userStartedRef.current += 1;
    askBeforeDiscarding(opener, () => setIsModalOpen(true));
  };
  const handleOpenModal = () => requestNewProof(document.activeElement instanceof HTMLElement ? document.activeElement : null);
  // The button of a finished proof goes away with the proof, so the toolbar button is the one that gets the focus back.
  const handleOpenModalFromMenu = () => requestNewProof(newProofButtonRef.current);
  const handleConfirmNewProof = () => {
    const proceed = pendingDiscard;
    setPendingDiscard(null);
    proceed?.();
  };
  const handleCancelNewProof = useCallback(() => {
    setPendingDiscard(null);
    modalOpener?.focus();
  }, [modalOpener]);
  const handleCloseModal = () => {
    dialogSessionRef.current += 1;
    setIsModalOpen(false);
  };

  // Escape cancels the inline confirmation, like the dialog does for itself.
  useEffect(() => {
    if (!confirmingNewProof) return;
    const handleKeyDown = (event: KeyboardEvent) => {
      if (event.key === 'Escape') handleCancelNewProof();
    };
    document.addEventListener('keydown', handleKeyDown);
    return () => document.removeEventListener('keydown', handleKeyDown);
  }, [confirmingNewProof, handleCancelNewProof]);

  // Cancel is the safe default action, so it gets the focus as soon as the confirmation appears.
  useEffect(() => {
    if (confirmingNewProof) confirmCancelRef.current?.focus();
  }, [confirmingNewProof]);

  // Replaces the proof on screen with a new one, and forgets everything about the previous one. `newExerciseId` is the
  // exercise the new proof was started from, if any.
  const showNewProof = (newProof: ProofDto, newExerciseId: string | null = null) => {
    setProof(newProof);
    setExerciseId(newExerciseId);
    setColorMapping(new Map<number, string>())
    setUndoError('');
    setRestoreError('');
    setExerciseStartError('');
    // Whatever a confirmation still on screen would go on to do was asked for about the previous proof.
    setPendingDiscard(null);
    proofIdRef.current += 1;
    setProofId(proofIdRef.current);
  };

  // The Menu's way to replace the proof (a rule applied, or the solver's answer). Its answers can arrive after the Menu
  // they belong to was replaced by a newer proof (New Proof, an exercise or "Try an example" can be started while a rule
  // or the solver is pending), and must not replace that newer proof: it would then be shown, saved and possibly marked
  // solved as the newer proof's exercise. Each Menu is keyed by `proofId`, so it gets the callback bound to its own proof.
  const setMenuProof = useCallback((updated: ProofDto) => {
    if (proofIdRef.current !== proofId) return;
    setProof(updated);
  }, [proofId]);

  // On mount, brings back the proof saved before a reload (see the effect below that saves it). It goes through the
  // backend's replay like any other proof, so that it is checked again and comes back with its `done` verdict; a proof
  // the backend rejects as invalid (a 400), or saved text that is not a proof, is forgotten and the empty state says so.
  // Any other failure (the backend unreachable or failing) says nothing about the proof, so it stays saved for the next
  // reload to try again. An answer arriving after the user started another proof meanwhile ("Try an example" and New
  // Proof stay usable) is dropped, and the saved proof is left as it is until that proof replaces it; so is one for an
  // unmounted App (React mounts twice in development).
  useEffect(() => {
    const saved = readSavedProof();
    if (saved.kind === 'none') return;
    if (saved.kind === 'corrupt') {
      clearSavedProof();
      setRestoreError('The proof saved before the page was reloaded could not be read, so it was discarded.');
      return;
    }
    let active = true;
    const requestedProofId = proofIdRef.current;
    const requestedStart = userStartedRef.current;
    setIsRestoring(true);
    replayProof(saved.proof.logic, saved.proof, (result) => {
      if (!active) return;
      setIsRestoring(false);
      if (proofIdRef.current !== requestedProofId) return;
      if (userStartedRef.current !== requestedStart) {
        // The user asked for another proof while this one was being restored (e.g. opened the New Proof dialog, which
        // did not ask to discard anything since nothing was on screen yet). Say so in the empty state, in case they
        // cancel; a proof they start replaces the notice and the saved proof.
        if (result.proof) {
          setRestoreError('The proof saved before the page was reloaded was not restored, since a new proof was started meanwhile. It is still saved: reload the page to restore it.');
        }
        return;
      }
      if (result.proof) {
        showNewProof(result.proof, saved.exerciseId);
      } else if (result.status === 400) {
        clearSavedProof();
        setRestoreError(`The proof saved before the page was reloaded could not be restored: ${result.message || 'the backend rejected it.'}`);
      } else {
        // Not a verdict on the proof (no connection, a server error, a busy server): keep it for the next reload.
        setRestoreError(`The proof saved before the page was reloaded could not be restored right now (${result.message}). It is still saved: reload the page to try again.`);
      }
    });
    return () => { active = false; };
    // Only on mount: `showNewProof` only calls state setters and reads refs.
  }, []);

  // Saves every change of the proof on screen (a new proof, a rule, an undo, a solve), for the effect above to restore
  // after a reload. The empty state is never saved, so that it cannot overwrite a saved proof that is still being
  // restored.
  useEffect(() => {
    if (proof.goal !== '' || proof.steps.length > 0) writeSavedProof(proof, exerciseId);
  }, [proof, exerciseId]);

  // A proof of an exercise that the backend says is done marks the exercise as solved, however it got there (rules, or
  // the solver). The stored ids are merged into the ones already known, so that when storage cannot be used the
  // exercises solved earlier in this page session still count.
  useEffect(() => {
    if (proof.done === true && exerciseId !== null) {
      const stored = markExerciseSolved(proof.logic, exerciseId);
      setSolvedThisSession((previous) => ({
        ...previous,
        [proof.logic]: [...new Set([...(previous[proof.logic] ?? []), ...stored])],
      }));
    }
  }, [proof, exerciseId]);

  // Fetches the exercises the first time they are needed: when the list is opened, or when a proof of an exercise is on
  // screen (also after a reload), since "Next exercise" needs the list. A failed fetch is only tried again when the list
  // is opened again (see `handleToggleExercises`). They are the exercises of the logic on screen, fetched again when it
  // changes; an answer for a logic that is no longer on screen is dropped.
  useEffect(() => {
    if (exercisesState !== null || (!isExercisesOpen && exerciseId === null)) return;
    const requested = logic;
    setExercisesOf({ logic: requested, state: { kind: 'loading' } });
    const settle = (state: ExercisesState) =>
      setExercisesOf((previous) => (previous?.logic === requested ? { logic: requested, state } : previous));
    fetchExercises(
      requested,
      (exercises) => settle({ kind: 'loaded', exercises }),
      (message) => settle({ kind: 'error', message }));
  }, [exercisesState, isExercisesOpen, exerciseId, logic]);

  // Has the backend check a proof of only its premises and goal (see `replayProof`, the path Undo uses: it parses the
  // goal and every step), since `checkFormula` is only an instant check that can drift from the backend's parser.
  // Resolves to the proof as the backend returned it, or to the reason it was refused.
  // In a logic with states (modal) the premises are in the initial state (see `premiseParameters`).
  const checkNewProof = (premises: string[], goal: string, proofLogic: string) => new Promise<{ proof: ProofDto } | { error: string }>((resolve) => {
    const steps: StepDto[] = premises.map((premise) => ({
      expression: premise.trim(),
      rule: 'Ass',
      assmsLevel: 0,
      extraParameters: premiseParameters(premise, proofLogic),
    }));
    replayProof(proofLogic, { steps: steps, logic: proofLogic, goal: goal }, (result) => resolve(result.proof
      ? { proof: result.proof }
      : { error: result.message || 'Could not start the proof.' }));
  });

  // Start Proof in the New Proof dialog: the proof is only shown once the backend accepted it, and a refusal keeps the
  // dialog open with the reason. An answer arriving after the dialog was closed is dropped.
  const handleNewProofSubmit = async (premises: string[], goal: string, proofLogic: string): Promise<string | null> => {
    const session = dialogSessionRef.current;
    const checked = await checkNewProof(premises, goal, proofLogic);
    if (dialogSessionRef.current !== session) return null;
    if ('error' in checked) return checked.error;
    showNewProof(checked.proof);
    return null;
  };

  // Starts the example exactly as if its premises and goal had been typed in the New Proof dialog, through the same
  // backend check. The button goes away with the empty state, so hand the focus to a control that stays on the page,
  // as `handleUndo` does. An answer arriving after another proof was started, or asked for (the dialog can be opened,
  // or an exercise started, meanwhile), is dropped.
  const handleTryExample = async () => {
    if (isStartingExample) return;
    userStartedRef.current += 1;
    const requestedStart = userStartedRef.current;
    setExampleError('');
    setIsStartingExample(true);
    const requestedProofId = proofIdRef.current;
    const checked = await checkNewProof(EXAMPLE_PREMISES, EXAMPLE_GOAL, logic);
    setIsStartingExample(false);
    if (proofIdRef.current !== requestedProofId) return;
    // That later request wins. It may still be cancelled (the New Proof dialog), and then nothing replaces the empty
    // state, so say there that the example was not started; a proof that is started replaces the empty state.
    if (userStartedRef.current !== requestedStart) {
      setExampleError('The example was not started, since a new proof was asked for meanwhile. Click "Try an example" again to start it.');
      return;
    }
    if ('error' in checked) {
      setExampleError(checked.error);
      return;
    }
    showNewProof(checked.proof);
    newProofButtonRef.current?.focus();
  };

  // "Start" in the list of exercises, and "Next exercise": starts the exercise exactly as if its premises and goal had
  // been typed in the New Proof dialog, through the same backend check, and remembers which exercise it is. A refusal
  // is shown in the list (opened if it was not). An answer arriving after another proof was started, or asked for, is
  // dropped. The Menu
  // stays usable while the backend checks the exercise, so if the proof on screen changed meanwhile (a rule, an undo)
  // and now has more than its premises, discarding it asks first, again, even if it was confirmed (or needed no
  // confirmation) when Start was clicked.
  const startExercise = async (exercise: Exercise) => {
    userStartedRef.current += 1;
    const requestedStart = userStartedRef.current;
    setExerciseStartError('');
    setStartingExerciseId(exercise.id);
    const requestedProofId = proofIdRef.current;
    const requestedProof = proofRef.current;
    // The list only shows the exercises of the logic on screen.
    const checked = await checkNewProof(exercise.premises, exercise.goal, logic);
    setStartingExerciseId(null);
    if (proofIdRef.current !== requestedProofId) return;
    // The user asked for another proof meanwhile (e.g. New Proof, which may be waiting for its own discard confirmation
    // or have its dialog open): that request wins. It may still be cancelled, and then nothing replaces the proof on
    // screen, so say in the list that the exercise was not started; a proof that is started replaces the notice.
    if (userStartedRef.current !== requestedStart) {
      setExerciseStartError(`The exercise "${exercise.title}" was not started, since a new proof was asked for meanwhile. Start it again to try it.`);
      setIsExercisesOpen(true);
      return;
    }
    if ('error' in checked) {
      setExerciseStartError(`The exercise "${exercise.title}" could not be started: ${checked.error}`);
      setIsExercisesOpen(true);
      return;
    }
    // Also run later, from the confirmation below: by then another proof may have been shown, or asked for, and that
    // one must not be replaced without asking about it.
    const show = () => {
      if (proofIdRef.current !== requestedProofId || userStartedRef.current !== requestedStart) return;
      showNewProof(checked.proof, exercise.id);
      setIsExercisesOpen(false);
      // The list, and the Menu button that may have started this, go away: hand the focus to a control that stays.
      exercisesButtonRef.current?.focus();
    };
    const current = proofRef.current;
    if (current !== requestedProof && current.steps.length > premiseCount(current.steps)) {
      setModalOpener(exercisesButtonRef.current);
      setPendingDiscard(() => show);
      return;
    }
    show();
  };

  const requestExercise = (exercise: Exercise, opener: HTMLElement | null) => {
    if (startingExerciseId !== null) return;
    askBeforeDiscarding(opener, () => { void startExercise(exercise); });
  };

  const handleToggleExercises = () => {
    if (isExercisesOpen) {
      setIsExercisesOpen(false);
      return;
    }
    setExerciseStartError('');
    // Opening the list again tries again to fetch exercises that could not be fetched.
    if (exercisesState?.kind === 'error') setExercisesOf(null);
    setIsExercisesOpen(true);
  };
  const handleBrowseExercises = () => {
    focusExercisesOnOpenRef.current = true;
    handleToggleExercises();
  };
  // See `focusExercisesOnOpenRef`.
  useEffect(() => {
    if (!isExercisesOpen || !focusExercisesOnOpenRef.current) return;
    focusExercisesOnOpenRef.current = false;
    document.getElementById('exercises-title')?.focus();
  }, [isExercisesOpen]);
  const handleCloseExercises = () => {
    setIsExercisesOpen(false);
    exercisesButtonRef.current?.focus();
  };

  const exercises = exercisesState?.kind === 'loaded' ? exercisesState.exercises : [];
  const currentExercise = exercises.find((exercise) => exercise.id === exerciseId);
  // The exercise after the current one in the list (which the backend orders from easy to hard), if any.
  const nextExercise = currentExercise ? exercises[exercises.indexOf(currentExercise) + 1] : undefined;
  const handleNextExercise = nextExercise
    ? () => requestExercise(nextExercise, exercisesButtonRef.current)
    : undefined;

  // "Load from text" in the New Proof dialog: the backend reads the text as a finished proof (see `loadProofFromText`),
  // which is only shown once it is accepted; a refusal keeps the dialog open with the reason. An answer arriving after
  // the dialog was closed is dropped.
  const handleLoadText = (text: string, proofLogic: string) => new Promise<string | null>((resolve) => {
    const session = dialogSessionRef.current;
    loadProofFromText(proofLogic, text, (result) => {
      if (dialogSessionRef.current !== session) {
        resolve(null);
      } else if (result.success && result.proof) {
        showNewProof(result.proof);
        resolve(null);
      } else {
        resolve(result.message || 'Could not load the proof.');
      }
    });
  });

  // Copies the proof in the text layout the backend reads back (see `proofToText`), for "Load from text" or a file. The
  // backend rejects a text that does not end at the top level and takes its last line as the goal, which the notice
  // says when this one is not done yet, or when its last line is not the goal (see `copyLoadNote`).
  const handleCopyText = async () => {
    const text = proofToText(proof);
    try {
      await navigator.clipboard.writeText(text);
      setCopyResult({ proof, text, copied: true });
    } catch {
      setCopyResult({ proof, text, copied: false });
    }
  };

  // Nothing to undo once only the premises are left, or there are no steps at all.
  const canUndo = proof.steps.length > premiseCount(proof.steps);

  const handleUndo = () => {
    if (!canUndo || isUndoing) return;
    setUndoError('');
    setIsUndoing(true);
    const requestedProofId = proofIdRef.current;
    undoLastStep(logic, proof, (result) => {
      setIsUndoing(false);
      // New Proof stays enabled while an undo is in flight, and it bumps `proofIdRef`. If that happened, this
      // response is about a proof that no longer exists on screen; applying it (or reporting its error) would
      // clobber the newer proof, so drop it.
      if (proofIdRef.current !== requestedProofId) return;
      if (result.proof) {
        const updated = result.proof;
        setProof(updated);
        setColorMapping(new Map<number, string>());
        // The button is about to become disabled; hand the focus somewhere that stays on the page.
        if (updated.steps.length <= premiseCount(updated.steps)) {
          newProofButtonRef.current?.focus();
        }
      } else {
        setUndoError(result.message || 'Could not undo the last step.');
      }
    });
  };

  const hasProof = proof.goal !== '' || proof.steps.length > 0;

  // The logic picked on the empty page, for "Try an example", the exercises and the New Proof dialog. There is no proof
  // to lose, so it just changes.
  const handleEmptyLogicChange = (value: string) => {
    setExampleError('');
    setExerciseStartError('');
    setProof((previous) => ({ ...previous, logic: value }));
  };

  return (
    <div className="App">
      {/* While the modal is open the page behind it can neither be tabbed to nor read by assistive technology. */}
      <div inert={isModalOpen}>
        <Header />
        <div className="app-toolbar">
          <button
            className="undo-btn"
            onClick={handleUndo}
            disabled={!canUndo || isUndoing}
            aria-disabled={!canUndo || isUndoing}
          >
            {isUndoing ? 'Undoing…' : 'Undo last step'}
          </button>
          <button
            className="copy-text-btn"
            onClick={handleCopyText}
            disabled={proof.steps.length === 0}
            aria-disabled={proof.steps.length === 0}
          >
            Copy proof as text
          </button>
          <button
            ref={exercisesButtonRef}
            className="exercises-btn"
            onClick={handleToggleExercises}
            aria-expanded={isExercisesOpen}
            aria-controls={isExercisesOpen ? 'exercises-panel' : undefined}
          >
            Exercises
          </button>
          <button
            ref={newProofButtonRef}
            className="new-proof-btn"
            onClick={handleOpenModal}
            aria-label="Start a new proof"
          >
            New Proof
          </button>
          {confirmingNewProof && (
            <div className="confirm-bar" role="alertdialog" aria-labelledby="confirm-new-proof-title" aria-describedby="confirm-new-proof-desc">
              <span id="confirm-new-proof-title" className="visually-hidden">Discard the current proof?</span>
              <span id="confirm-new-proof-desc">Starting a new proof discards the current one. Continue?</span>
              <button className="confirm-btn" onClick={handleConfirmNewProof}>Discard and start new</button>
              <button ref={confirmCancelRef} className="cancel-btn" onClick={handleCancelNewProof}>Cancel</button>
            </div>
          )}
        </div>
        {undoError && (
          <p className="undo-error" role="alert" aria-live="assertive">{undoError}</p>
        )}
        {currentCopy && <CopyStatus copy={currentCopy} />}
        {isExercisesOpen && (
          <ExerciseList
            logic={logic}
            state={exercisesState ?? { kind: 'loading' }}
            solved={solved}
            currentId={exerciseId}
            startingId={startingExerciseId}
            startError={exerciseStartError}
            onStart={(exercise) => requestExercise(exercise, document.activeElement instanceof HTMLElement ? document.activeElement : null)}
            onClose={handleCloseExercises}
          />
        )}
        <main className="app-main">
          {currentExercise && (
            <p className="current-exercise">
              Exercise: <strong>{currentExercise.title}</strong>{solved.has(currentExercise.id) ? ' (Solved)' : ''}
            </p>
          )}
          <Menu key={proofId} ref={menuRef} logic={logic} proof={proof} setProof={setMenuProof} onColorChange={onColorChange} onNewProof={handleOpenModalFromMenu} onNextExercise={handleNextExercise} />
          {hasProof ? (
            <Proof proof={proof} coloring={colorMapping} onSelectLine={handleSelectLine} />
          ) : (
            <div className="empty-proof-state">
              <output className="paragraph" aria-live="polite">
                {isRestoring
                  ? 'Restoring the proof from before the page was reloaded…'
                  : <>No proof loaded. Click <strong>New Proof</strong> to begin.</>}
              </output>
              {restoreError && <p className="restore-error" role="alert">{restoreError}</p>}
              <div className="empty-logic">
                <label htmlFor="empty-logic-select" className="empty-logic-label">Logic:</label>
                <select
                  id="empty-logic-select"
                  value={logic}
                  onChange={(e) => handleEmptyLogicChange(e.target.value)}
                  disabled={isStartingExample || startingExerciseId !== null}
                  aria-describedby="empty-logic-desc"
                >
                  {LOGICS.map((info) => <option key={info.id} value={info.id}>{info.name}</option>)}
                </select>
                <p id="empty-logic-desc" className="empty-logic-desc">
                  {logicInfo(logic)?.description} The example and the exercises use this logic.
                </p>
              </div>
              <h2 className="how-it-works-title">How it works</h2>
              <ol className="how-it-works">
                <li>Enter the premises and the goal of the proof.</li>
                <li>Pick an inference rule.</li>
                <li>Enter the line numbers the rule uses, and apply it.</li>
              </ol>
              <button className="try-example-btn" onClick={handleTryExample} disabled={isStartingExample}>
                {isStartingExample ? 'Starting the example…' : 'Try an example'}
              </button>
              {exampleError && <p className="try-example-error" role="alert">{exampleError}</p>}
              <p className="try-example-desc">
                Premises <code>{EXAMPLE_PREMISES.join(', ')}</code>, goal <code>{EXAMPLE_GOAL}</code>.
              </p>
              {!isExercisesOpen && (
                <p className="browse-exercises">
                  Or practise with a graded exercise:{' '}
                  <button className="browse-exercises-btn" onClick={handleBrowseExercises}>Browse exercises</button>
                </p>
              )}
            </div>
          )}
        </main>
      </div>

      <NewProofModal
        isOpen={isModalOpen}
        opener={modalOpener}
        onClose={handleCloseModal}
        logic={logic}
        onSubmit={handleNewProofSubmit}
        onLoadText={handleLoadText}
      />
    </div>
  );
}

export default App;
