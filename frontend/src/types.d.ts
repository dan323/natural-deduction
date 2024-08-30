export type ProofDto = {
    steps: Array<StepDto>,
    logic: string,
    goal: string,
};

export type StepDto = {
    expression: string,
    rule: string,
    assmsLevel: number,
    extraParameters: Map<string, string>,
}

export type ActionDto = {
    name: string,
    sources: Array<number>,
    extraParameters: any,
}

export type ApplyActionResponse = {
    proof: ProofDto,
    success: boolean,
    message: string,
}