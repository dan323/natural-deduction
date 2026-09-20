export type ProofDto = {
    steps: Array<StepDto>,
    logic: string,
    goal: string,
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
    message: string,
}

// The kind of input an action needs; the names are those of the backend's ParamKind.
export type ParamKind = 'INT' | 'EXPRESSION' | 'STATE';

// One action of `GET /logic/{logic}/actions`: `name` goes in ActionDto.name, `params` lists the inputs in order.
export type ActionDescriptor = {
    name: string,
    params: Array<ParamKind>,
}
