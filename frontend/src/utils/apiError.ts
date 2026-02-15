import axios from "axios";

type ApiErrorResponse = {
  message?: string;
  errors?: string[];
  fieldErrors?: Record<string, string[]>;
};

const isObject = (v: unknown): v is Record<string, unknown> =>
  typeof v === "object" && v !== null;

export const getApiErrorMessageFromBody = (
  body: unknown,
  fallback = "Unknown error occurred",
): string => {
  if (!isObject(body)) return fallback;

  const b = body as ApiErrorResponse;

  if (b.errors?.length) return b.errors.join(", ");
  return b.message?.trim() || fallback;
};

export const getApiErrorMessage = (
  error: unknown,
  fallback = "Unknown error occurred",
): string => {
  if (!axios.isAxiosError(error)) {
    return error instanceof Error ? error.message.trim() || fallback : fallback;
  }

  const data = error.response?.data;
  const axiosMessage = (error.message || "").trim() || fallback;

  if (!isObject(data)) return axiosMessage;

  return getApiErrorMessageFromBody(data, axiosMessage);
};

export const getApiFieldErrorsFromBody = (
  body: unknown,
): Record<string, string[]> => {
  if (!isObject(body)) return {};
  const b = body as ApiErrorResponse;
  return b.fieldErrors ?? {};
};

export const getApiFieldErrors = (error: unknown): Record<string, string[]> => {
  if (!axios.isAxiosError(error)) return {};
  return getApiFieldErrorsFromBody(error.response?.data);
};
