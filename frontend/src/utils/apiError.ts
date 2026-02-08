import { AxiosError } from "axios";

type ApiErrorResponse = {
	message?: string;
	errors?: string[];
	fieldErrors?: Record<string, string[]>;
};

const isApiErrorResponse = (value: unknown): value is ApiErrorResponse => {
	return typeof value === "object" && value !== null;
};

export const getApiErrorMessageFromBody = (
	body: unknown,
	fallback = "Unknown error occurred",
): string => {
	if (!isApiErrorResponse(body)) {
		return fallback;
	}

	if (body.errors?.length) {
		return body.errors.join(", ");
	}

	return body.message?.trim() || fallback;
};

export const getApiErrorMessage = (
	error: unknown,
	fallback = "Unknown error occurred",
): string => {
	if (!(error instanceof AxiosError)) {
		return error instanceof Error ? error.message : fallback;
	}

	const data = error.response?.data;
	if (!isApiErrorResponse(data)) {
		return error.message || fallback;
	}

	return getApiErrorMessageFromBody(data, error.message || fallback);
};

export const getApiFieldErrors = (error: unknown): Record<string, string[]> => {
	if (!(error instanceof AxiosError)) return {};
	
	const data = error.response?.data;
	return isApiErrorResponse(data) && data.fieldErrors ? data.fieldErrors : {};
};
