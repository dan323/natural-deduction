import { fetchActions, applyAction, solveProof } from '../actions';
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
  });
});
