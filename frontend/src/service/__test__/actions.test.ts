import { fetchActions, applyAction, solveProof, clearActionsCache, loadProofFromText } from '../actions';
import { ProofDto, ActionDto } from '../../types';

const proof: ProofDto = { steps: [], logic: 'classical', goal: 'P' };
const action: ActionDto = { name: 'Rep', sources: [1], extraParameters: { expression: '' } };

function jsonResponse(status: number, body: unknown): Response {
  return { ok: status >= 200 && status < 300, status, json: async () => body } as Response;
}

function textResponse(status: number): Response {
  return {
    ok: false,
    status,
    json: async () => { throw new SyntaxError('Unexpected token <'); },
  } as unknown as Response;
}

describe('service/actions', () => {
  const fetchMock = jest.fn();
  let consoleError: jest.SpyInstance;

  beforeEach(() => {
    fetchMock.mockReset();
    clearActionsCache();
    (global as any).fetch = fetchMock;
    consoleError = jest.spyOn(console, 'error').mockImplementation(() => undefined);
  });

  afterEach(() => {
    consoleError.mockRestore();
  });

  describe('applyAction', () => {
    test('passes a successful response through', async () => {
      const body = { proof, success: true, message: '' };
      fetchMock.mockResolvedValue(jsonResponse(200, body));
      const consumer = jest.fn();

      await applyAction('classical', proof, action, consumer);

      expect(consumer).toHaveBeenCalledWith(body);
    });

    test('carries the done verdict of the response on the returned proof', async () => {
      fetchMock.mockResolvedValue(jsonResponse(200, { proof, success: true, done: true, message: '' }));
      const consumer = jest.fn();

      await applyAction('classical', proof, action, consumer);

      expect(consumer).toHaveBeenCalledWith({ proof: { ...proof, done: true }, success: true, done: true, message: '' });
    });

    test('a 202 keeps the reason the action was not applied', async () => {
      const body = { proof, success: false, message: 'Line 1 does not exist' };
      fetchMock.mockResolvedValue(jsonResponse(202, body));
      const consumer = jest.fn();

      await applyAction('classical', proof, action, consumer);

      expect(consumer).toHaveBeenCalledWith(body);
    });

    test('a 500 surfaces the message of the error body', async () => {
      fetchMock.mockResolvedValue(jsonResponse(500, { message: 'Something went wrong' }));
      const consumer = jest.fn();

      await applyAction('classical', proof, action, consumer);

      expect(consumer).toHaveBeenCalledWith({ success: false, message: 'Something went wrong' });
    });

    test('an error without a JSON body falls back to the status', async () => {
      fetchMock.mockResolvedValue(textResponse(502));
      const consumer = jest.fn();

      await applyAction('classical', proof, action, consumer);

      expect(consumer).toHaveBeenCalledWith({ success: false, message: 'Request failed with status 502.' });
    });

    test('a network failure reports a network error', async () => {
      fetchMock.mockRejectedValue(new TypeError('Failed to fetch'));
      const consumer = jest.fn();

      await applyAction('classical', proof, action, consumer);

      expect(consumer).toHaveBeenCalledWith({ success: false, message: 'Network error. Please try again.' });
    });
  });

  describe('solveProof', () => {
    test('posts the proof and passes the solved proof on', async () => {
      const solved = { ...proof, steps: [{ expression: 'P', rule: 'Ass', assmsLevel: 0, extraParameters: {} }] };
      fetchMock.mockResolvedValue(jsonResponse(200, { ...solved, done: true }));
      const consumer = jest.fn();

      await solveProof('classical', proof, consumer);

      expect(fetchMock).toHaveBeenCalledWith('/logic/classical/solve', expect.objectContaining({
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(proof),
      }));
      expect(consumer).toHaveBeenCalledWith({ success: true, proof: { ...solved, done: true }, message: '' });
    });

    test('a 422 surfaces the message of the error body', async () => {
      fetchMock.mockResolvedValue(jsonResponse(422, { message: 'The solver did not finish within 10 seconds' }));
      const consumer = jest.fn();

      await solveProof('classical', proof, consumer);

      expect(consumer).toHaveBeenCalledWith({ success: false, message: 'The solver did not finish within 10 seconds' });
    });

    test('an error without a JSON body falls back to the status', async () => {
      fetchMock.mockResolvedValue(textResponse(502));
      const consumer = jest.fn();

      await solveProof('classical', proof, consumer);

      expect(consumer).toHaveBeenCalledWith({ success: false, message: 'Request failed with status 502.' });
    });

    test('gives up on a request that never answers', async () => {
      jest.useFakeTimers();
      try {
        fetchMock.mockImplementation((url: string, init: RequestInit) => new Promise((resolve, reject) => {
          init.signal?.addEventListener('abort', () => reject(new DOMException('Aborted', 'AbortError')));
        }));
        const consumer = jest.fn();

        const solving = solveProof('classical', proof, consumer);
        expect(consumer).not.toHaveBeenCalled();
        jest.advanceTimersByTime(60_000);
        await solving;

        expect(consumer).toHaveBeenCalledWith({ success: false, message: 'The solver took too long to answer. Please try again.' });
      } finally {
        jest.useRealTimers();
      }
    });

    test('a network failure reports a network error', async () => {
      fetchMock.mockRejectedValue(new TypeError('Failed to fetch'));
      const consumer = jest.fn();

      await solveProof('classical', proof, consumer);

      expect(consumer).toHaveBeenCalledWith({ success: false, message: 'Network error. Please try again.' });
    });
  });

  describe('fetchActions', () => {
    test('passes the actions to the consumer', async () => {
      fetchMock.mockResolvedValue(jsonResponse(200, [{ name: 'Rep', params: ['INT'] }]));
      const consumer = jest.fn();
      const onError = jest.fn();

      await fetchActions('classical', consumer, onError);

      expect(fetchMock).toHaveBeenCalledWith('/logic/classical/actions');
      expect(consumer).toHaveBeenCalledWith([{ name: 'Rep', params: ['INT'] }]);
      expect(onError).not.toHaveBeenCalled();
    });

    test('reports the message of an error response', async () => {
      fetchMock.mockResolvedValue(jsonResponse(404, { message: 'Unknown logic: foo' }));
      const consumer = jest.fn();
      const onError = jest.fn();

      await fetchActions('foo', consumer, onError);

      expect(consumer).not.toHaveBeenCalled();
      expect(onError).toHaveBeenCalledWith('Unknown logic: foo');
    });

    test('reports a network failure', async () => {
      fetchMock.mockRejectedValue(new TypeError('Failed to fetch'));
      const onError = jest.fn();

      await fetchActions('classical', jest.fn(), onError);

      expect(onError).toHaveBeenCalledWith('Failed to fetch');
    });

    test('shares one request between concurrent and later calls for the same logic', async () => {
      fetchMock.mockResolvedValue(jsonResponse(200, [{ name: 'Rep', params: ['INT'] }]));
      const first = jest.fn();
      const second = jest.fn();
      const third = jest.fn();

      await Promise.all([fetchActions('classical', first), fetchActions('classical', second)]);
      await fetchActions('classical', third);

      expect(fetchMock).toHaveBeenCalledTimes(1);
      for (const consumer of [first, second, third]) {
        expect(consumer).toHaveBeenCalledWith([{ name: 'Rep', params: ['INT'] }]);
      }
    });

    test('requests each logic separately', async () => {
      fetchMock.mockResolvedValue(jsonResponse(200, []));

      await fetchActions('classical', jest.fn());
      await fetchActions('modal', jest.fn());

      expect(fetchMock).toHaveBeenCalledTimes(2);
      expect(fetchMock).toHaveBeenCalledWith('/logic/modal/actions');
    });

    test('a failed request is not cached', async () => {
      fetchMock.mockRejectedValueOnce(new TypeError('Failed to fetch'));
      fetchMock.mockResolvedValueOnce(jsonResponse(200, [{ name: 'Rep', params: ['INT'] }]));
      const onError = jest.fn();
      const consumer = jest.fn();

      await fetchActions('classical', jest.fn(), onError);
      await fetchActions('classical', consumer, onError);

      expect(onError).toHaveBeenCalledTimes(1);
      expect(fetchMock).toHaveBeenCalledTimes(2);
      expect(consumer).toHaveBeenCalledWith([{ name: 'Rep', params: ['INT'] }]);
    });

    test('an error response is not cached either', async () => {
      fetchMock.mockResolvedValueOnce(jsonResponse(500, { message: 'Boom' }));
      fetchMock.mockResolvedValueOnce(jsonResponse(200, []));
      const onError = jest.fn();
      const consumer = jest.fn();

      await fetchActions('classical', jest.fn(), onError);
      await fetchActions('classical', consumer, onError);

      expect(onError).toHaveBeenCalledWith('Boom');
      expect(consumer).toHaveBeenCalledWith([]);
    });
  });

  describe('loadProofFromText', () => {
    const text = ['P           Ass', '   Q           Ass', '   P           Rep [1]', 'Q -> P           ->I [2-3]'].join('\n');
    const parsed: ProofDto = {
      steps: [
        { expression: 'P', rule: 'Ass', assmsLevel: 0, extraParameters: {} },
        { expression: 'Q', rule: 'Ass', assmsLevel: 1, extraParameters: {} },
        { expression: 'P', rule: 'Rep [1]', assmsLevel: 1, extraParameters: {} },
        { expression: 'Q -> P', rule: '->I [2-3]', assmsLevel: 0, extraParameters: {} },
      ],
      logic: 'classical',
      goal: 'Q -> P',
    };

    const uploadedText = async (call: unknown[]) => {
      const [, init] = call as [string, RequestInit];
      const file = (init.body as FormData).get('file') as File;
      expect(file.name).toBe('proof.txt');
      // jsdom's Blob has no text(); FileReader reads it the same way.
      return new Promise<string>((resolve) => {
        const reader = new FileReader();
        reader.onload = () => resolve(reader.result as string);
        reader.readAsText(file);
      });
    };

    test('uploads the text as the file of POST .../proof, then replays the result for its done verdict', async () => {
      fetchMock
        .mockResolvedValueOnce(jsonResponse(201, parsed))
        .mockResolvedValueOnce(jsonResponse(202, { proof: parsed, success: false, done: true, message: 'Line 5 does not exist' }));
      const consumer = jest.fn();

      await loadProofFromText('classical', text + '\n\n', '', consumer);

      expect(fetchMock).toHaveBeenCalledTimes(2);
      const [url, init] = fetchMock.mock.calls[0];
      expect(url).toBe('/logic/classical/proof');
      expect(init.method).toBe('POST');
      expect(init.body).toBeInstanceOf(FormData);
      // Trailing blank lines are dropped, the rest is sent as is.
      expect(await uploadedText(fetchMock.mock.calls[0])).toBe(text);

      const [replayUrl, replayInit] = fetchMock.mock.calls[1];
      expect(replayUrl).toBe('/logic/classical/action');
      expect(JSON.parse(replayInit.body)).toEqual({
        actionDto: { name: 'COPY', sources: [5], extraParameters: {} },
        proofDto: parsed,
      });
      expect(consumer).toHaveBeenCalledWith({ success: true, proof: { ...parsed, done: true }, done: true, message: '' });
    });

    test('a goal replaces the one the backend took from the last line', async () => {
      const unfinished = { ...parsed, goal: 'R' };
      fetchMock
        .mockResolvedValueOnce(jsonResponse(201, parsed))
        .mockResolvedValueOnce(jsonResponse(202, { proof: unfinished, success: false, done: false, message: '' }));
      const consumer = jest.fn();

      await loadProofFromText('classical', text, '  R ', consumer);

      expect(JSON.parse(fetchMock.mock.calls[1][1].body).proofDto.goal).toBe('R');
      expect(consumer).toHaveBeenCalledWith({ success: true, proof: { ...unfinished, done: false }, done: false, message: '' });
    });

    test('a text the backend cannot read keeps its reason, and nothing is replayed', async () => {
      fetchMock.mockResolvedValueOnce(jsonResponse(400, { message: 'Line 2 is not valid: unknown rule' }));
      const consumer = jest.fn();

      await loadProofFromText('classical', text, '', consumer);

      expect(fetchMock).toHaveBeenCalledTimes(1);
      expect(consumer).toHaveBeenCalledWith({ success: false, message: 'Line 2 is not valid: unknown rule' });
    });

    test('a goal the replay rejects is reported', async () => {
      fetchMock
        .mockResolvedValueOnce(jsonResponse(201, parsed))
        .mockResolvedValueOnce(jsonResponse(400, { message: 'The proof could not be read' }));
      const consumer = jest.fn();

      await loadProofFromText('classical', text, 'P &', consumer);

      expect(consumer).toHaveBeenCalledWith({ success: false, message: 'The proof could not be read' });
    });

    test('a network error is reported', async () => {
      fetchMock.mockRejectedValueOnce(new TypeError('Failed to fetch'));
      const consumer = jest.fn();

      await loadProofFromText('classical', text, '', consumer);

      expect(consumer).toHaveBeenCalledWith({ success: false, message: 'Network error. Please try again.' });
    });
  });
});
