import { ActionDescriptor, ProofDto, StepDto } from '../types';

// A mocked backend of a logic whose steps are in states (`modal`, `modal-next-until`), shared by the App tests of those
// logics.

export function jsonResponse(status: number, body: unknown): Response {
  return { ok: status >= 200 && status < 300, status, json: async () => body } as Response;
}

export const ASSUME: ActionDescriptor = { name: 'Ass', params: ['EXPRESSION', 'STATE'] };

// Makes `fetchMock` answer as the backend of such a logic: `actions` are its rules (the exercises are none); a replay
// (the COPY that New Proof sends) rejects a premise other than a relation that is not in `s0`, as
// `ModalProofTransformer.initialAssumption` does; the rule named `name` in `ruleSteps` adds `ruleSteps[name]`, not done;
// and `Ass` assumes its expression in its state.
export function mockStatesBackend(fetchMock: jest.Mock, actions: ActionDescriptor[], ruleSteps: Record<string, StepDto>) {
  fetchMock.mockImplementation(async (url: string, init?: RequestInit) => {
    if (url.endsWith('/actions')) return jsonResponse(200, actions);
    if (url.endsWith('/exercises')) return jsonResponse(200, []);
    const { proofDto, actionDto } = JSON.parse(init!.body as string) as { proofDto: ProofDto, actionDto: { name: string, extraParameters: Record<string, string> } };
    const badPremise = proofDto.steps.findIndex((step) =>
      step.rule === 'Ass' && step.assmsLevel === 0 && !step.expression.includes('=') && step.extraParameters.state !== 's0');
    if (badPremise !== -1) {
      return jsonResponse(400, { message: `Line ${badPremise + 1} is not valid: the assumptions are not in a valid state` });
    }
    const withStep = (step: StepDto) => {
      const proof = { ...proofDto, steps: [...proofDto.steps, step] };
      return jsonResponse(200, { proof, success: true, done: false, message: '' });
    };
    if (actionDto.name === 'COPY') return jsonResponse(202, { proof: proofDto, success: false, done: false, message: 'out of range' });
    if (actionDto.name === 'Ass') {
      return withStep({
        expression: actionDto.extraParameters.expression, rule: 'Ass', assmsLevel: 1,
        extraParameters: { state: actionDto.extraParameters.state },
      });
    }
    const step = ruleSteps[actionDto.name];
    return step ? withStep(step) : jsonResponse(400, { message: 'unexpected action' });
  });
}

// The bodies of the requests `fetchMock` got for `path`.
export function requestsTo(fetchMock: jest.Mock, path: string) {
  return fetchMock.mock.calls
    .filter(([url]) => String(url) === path)
    .map(([, init]) => JSON.parse(String(init.body)));
}
