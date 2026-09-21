export type ProofDto = {
    steps: Array<StepDto>,
    logic: string,
    goal: string,
    // Whether the goal is proved, as decided by the backend (`Proof.isDone()`); absent on a proof that was not returned by it.
    done?: boolean,
};

export type StepDto = {
    expression: string,
    rule: string,
    assmsLevel: number,
    extraParameters: Record<string, string>,
}

export type ActionDto = {
    name: string,
    sources: Array<number>,
    extraParameters: any,
}

export type ApplyActionResponse = {
    proof?: ProofDto,
    success: boolean,
    // Whether the goal is proved, as decided by the backend. Also set on `proof.done`.
    done?: boolean,
    message: string,
}

// The kind of input an action needs; the names are those of the backend's ParamKind.
export type ParamKind = 'INT' | 'EXPRESSION' | 'STATE';

// How the backend groups a rule; the names are those of its ActionCategory.
export type ActionCategory = 'INTRODUCTION' | 'ELIMINATION' | 'OTHER';

// One action of `GET /logic/{logic}/actions`: `name` goes in ActionDto.name, `params` lists the inputs in order.
// The other fields only help to present the action and are optional (a logic may send null, or leave them out, and
// an older backend does not know them): `label` is the human name ("Modus ponens"), `symbol` how the proof table shows
// the rule ("→E"), `description` one sentence saying what it does, and `paramLabels` one label per entry of `params`.
export type ActionDescriptor = {
    name: string,
    params: Array<ParamKind>,
    label?: string | null,
    symbol?: string | null,
    category?: ActionCategory | null,
    description?: string | null,
    paramLabels?: Array<string> | null,
}
