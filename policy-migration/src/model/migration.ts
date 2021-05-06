import { DittoMessage } from "./base.ts";

export type ModifySubjectCommand = DittoMessage & {
  value: {
    type: string;
  };
};

export type DeleteSubjectCommand = DittoMessage;
