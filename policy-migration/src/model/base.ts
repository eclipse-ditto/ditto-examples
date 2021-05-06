export type StringMap = {
  [key: string]: string;
};

export type DittoMessage = {
  topic: string;
  headers: StringMap;
  path: string;
  value?: unknown;
};

export type DittoResponse = DittoMessage & {
  status: number;
};

export type DittoErrorResponse = DittoResponse & {
  value: {
    status: number;
    error: string;
    message: string;
    description?: string;
    href?: string;
  };
};
