import {
	type ChangeEvent,
	type FormEvent,
	type ReactElement,
	useCallback,
	useEffect,
	useId,
	useState,
} from "react";
import { useNavigate } from "react-router-dom";
import {
	getApiErrorMessage,
	getApiErrorMessageFromBody,
	getApiFieldErrorsFromBody,
} from "../../utils/apiError";
import AccountInfoSection from "./AccountInfoSection";
import NotificationSettingsSection from "./NotificationSettingsSection";
import PasswordSection from "./PasswordSection";
import type {
	NotificationForm,
	NotificationSettingsResponse,
	PasswordField,
	PasswordFieldErrors,
	PasswordForm,
} from "./types";

const isPasswordField = (field: string): field is PasswordField =>
	field === "currentPassword" ||
	field === "newPassword" ||
	field === "confirmPassword";

const createEmptyPasswordFieldErrors = (): PasswordFieldErrors => ({
	currentPassword: [],
	newPassword: [],
	confirmPassword: [],
});

const createInitialNotificationForm = (): NotificationForm => ({
	enabled: false,
	topic: "",
});

const mapApiFieldErrorsToPasswordFields = (
	apiFieldErrors: Record<string, string[]>,
): PasswordFieldErrors => ({
	currentPassword: apiFieldErrors["currentPassword"] ?? [],
	newPassword: apiFieldErrors["newPassword"] ?? [],
	confirmPassword: apiFieldErrors["confirmPassword"] ?? [],
});

const MyAccount = (): ReactElement => {
	const [userName, setUserName] = useState<string>("");
	const [email, setEmail] = useState<string>("");
	const [loading, setLoading] = useState<boolean>(true);
	const [error, setError] = useState<string | null>(null);
	const [showPasswordForm, setShowPasswordForm] = useState<boolean>(false);
	const [passwordChangeError, setPasswordChangeError] = useState<string | null>(
		null,
	);
	const [passwordChangeSuccess, setPasswordChangeSuccess] = useState<
		string | null
	>(null);
	const [isChangingPassword, setIsChangingPassword] = useState<boolean>(false);
	const [passwordForm, setPasswordForm] = useState<PasswordForm>({
		currentPassword: "",
		newPassword: "",
		confirmPassword: "",
	});
	const [passwordFieldErrors, setPasswordFieldErrors] =
		useState<PasswordFieldErrors>(createEmptyPasswordFieldErrors());
	const [notificationLoading, setNotificationLoading] = useState<boolean>(true);
	const [notificationSaving, setNotificationSaving] = useState<boolean>(false);
	const [notificationTesting, setNotificationTesting] =
		useState<boolean>(false);
	const [notificationError, setNotificationError] = useState<string | null>(
		null,
	);
	const [notificationSuccess, setNotificationSuccess] = useState<string | null>(
		null,
	);
	const [copySubscribeUrlFeedback, setCopySubscribeUrlFeedback] = useState<
		string | null
	>(null);
	const [reminderMinutesBeforeDue, setReminderMinutesBeforeDue] =
		useState<number>(30);
	const [notificationForm, setNotificationForm] = useState<NotificationForm>(
		createInitialNotificationForm,
	);
	const [ntfyTopicPrefix, setNtfyTopicPrefix] = useState<string>("");
	const appNtfyProxyUrl =
		typeof window === "undefined" ? "/ntfy" : `${window.location.origin}/ntfy`;
	const [publicNtfyBaseUrl, setPublicNtfyBaseUrl] =
		useState<string>(appNtfyProxyUrl);
	const navigate = useNavigate();

	const getAuthTokenOrRedirect = useCallback((): string | null => {
		const token = localStorage.getItem("token");
		if (!token) {
			navigate("/login");
			return null;
		}

		return token;
	}, [navigate]);

	const createAuthHeaders = useCallback(
		(token: string, includeJsonContentType = false): HeadersInit => ({
			...(includeJsonContentType ? { "Content-Type": "application/json" } : {}),
			Authorization: `Bearer ${token}`,
		}),
		[],
	);

	const userNameId = useId();
	const emailId = useId();
	const currentPasswordId = useId();
	const newPasswordId = useId();
	const confirmPasswordId = useId();
	const ntfyTopicId = useId();
	const trimmedNtfyTopic = notificationForm.topic.trim();
	const ntfyTopicPrefixDisplay =
		ntfyTopicPrefix.length > 0 ? ntfyTopicPrefix : "tm-<user-id>-";
	const ntfySubscribeTopic =
		trimmedNtfyTopic.length > 0
			? `${ntfyTopicPrefix}${trimmedNtfyTopic}`
			: null;
	const ntfyTopicPreview =
		ntfySubscribeTopic ?? `${ntfyTopicPrefixDisplay}your-topic-name`;
	const ntfySubscribeUrl =
		ntfySubscribeTopic != null
			? `${publicNtfyBaseUrl}/${encodeURIComponent(ntfySubscribeTopic)}`
			: null;

	const applyNotificationSettings = useCallback(
		(data: unknown): void => {
			if (typeof data !== "object" || data === null) {
				return;
			}

			const settings = data as NotificationSettingsResponse;

			setNotificationForm({
				enabled: settings.enabled ?? false,
				topic: typeof settings.topic === "string" ? settings.topic : "",
			});
			setNtfyTopicPrefix(
				typeof settings.topicPrefix === "string" ? settings.topicPrefix : "",
			);
			setPublicNtfyBaseUrl(
				typeof settings.publicUrl === "string" &&
					settings.publicUrl.trim().length > 0
					? settings.publicUrl.trim().replace(/\/+$/, "")
					: appNtfyProxyUrl,
			);
			setReminderMinutesBeforeDue(settings.reminderMinutesBeforeDue ?? 30);
		},
		[appNtfyProxyUrl],
	);

	useEffect(() => {
		const fetchCurrentUser = async (): Promise<void> => {
			try {
				const token = getAuthTokenOrRedirect();
				if (token == null) {
					return;
				}

				const authHeaders = createAuthHeaders(token);

				const response = await fetch("/api/auth/current-user", {
					headers: authHeaders,
				});

				if (!response.ok) {
					const body: unknown = await response.json().catch(() => null);
					throw new Error(
						getApiErrorMessageFromBody(body, "Failed to fetch user"),
					);
				}

				const data: unknown = await response.json();
				if (typeof data === "object" && data !== null) {
					const user = data as { userName?: string; email?: string };
					setUserName(user.userName ?? "");
					setEmail(user.email ?? "");
				}

				const notificationResponse = await fetch(
					"/api/notifications/settings",
					{
						headers: authHeaders,
					},
				);

				if (!notificationResponse.ok) {
					const notificationData: unknown = await notificationResponse
						.json()
						.catch(() => null);
					setNotificationError(
						getApiErrorMessageFromBody(
							notificationData,
							"Failed to fetch notification settings",
						),
					);
					return;
				}

				const notificationData: unknown = await notificationResponse
					.json()
					.catch(() => null);
				applyNotificationSettings(notificationData);
			} catch (fetchError) {
				setError(getApiErrorMessage(fetchError, "Failed to fetch user"));
			} finally {
				setLoading(false);
				setNotificationLoading(false);
			}
		};

		void fetchCurrentUser();
	}, [applyNotificationSettings, createAuthHeaders, getAuthTokenOrRedirect]);

	const handlePasswordChange = async (
		e: FormEvent<HTMLFormElement>,
	): Promise<void> => {
		e.preventDefault();

		const token = getAuthTokenOrRedirect();
		if (token == null) {
			return;
		}

		setIsChangingPassword(true);
		setPasswordChangeError(null);
		setPasswordChangeSuccess(null);
		setPasswordFieldErrors(createEmptyPasswordFieldErrors());

		try {
			const response = await fetch("/api/auth/change-password", {
				method: "POST",
				headers: createAuthHeaders(token, true),
				body: JSON.stringify({
					currentPassword: passwordForm.currentPassword,
					newPassword: passwordForm.newPassword,
					confirmPassword: passwordForm.confirmPassword,
				}),
			});

			if (!response.ok) {
				const data: unknown = await response.json().catch(() => null);
				const fieldErrors = mapApiFieldErrorsToPasswordFields(
					getApiFieldErrorsFromBody(data),
				);
				if (Object.values(fieldErrors).some((errors) => errors.length > 0)) {
					setPasswordFieldErrors(fieldErrors);
					return;
				}

				throw new Error(
					getApiErrorMessageFromBody(data, "Failed to change password"),
				);
			}

			setPasswordChangeSuccess("Password changed successfully");
			setPasswordForm({
				currentPassword: "",
				newPassword: "",
				confirmPassword: "",
			});
			setPasswordFieldErrors(createEmptyPasswordFieldErrors());
			setShowPasswordForm(false);
		} catch (changeError) {
			setPasswordChangeError(
				getApiErrorMessage(changeError, "Failed to change password"),
			);
		} finally {
			setIsChangingPassword(false);
		}
	};

	const handlePasswordInputChange = (
		e: ChangeEvent<HTMLInputElement>,
	): void => {
		const { name, value } = e.target;
		if (!isPasswordField(name)) {
			return;
		}

		setPasswordForm((prev) => ({ ...prev, [name]: value }));
		setPasswordChangeError(null);
		setPasswordChangeSuccess(null);
		setPasswordFieldErrors(createEmptyPasswordFieldErrors());
	};

	const handleClearPasswordForm = (): void => {
		setPasswordForm({
			currentPassword: "",
			newPassword: "",
			confirmPassword: "",
		});
		setPasswordFieldErrors(createEmptyPasswordFieldErrors());
		setPasswordChangeError(null);
	};

	const handleCancelPasswordChange = (): void => {
		handleClearPasswordForm();
		setShowPasswordForm(false);
	};

	const handleOpenPasswordForm = (): void => {
		handleClearPasswordForm();
		setShowPasswordForm(true);
	};

	const handleNotificationInputChange = (
		e: ChangeEvent<HTMLInputElement>,
	): void => {
		const { name, value, checked } = e.target;
		setNotificationError(null);
		setNotificationSuccess(null);
		setCopySubscribeUrlFeedback(null);

		setNotificationForm((prev) => {
			switch (name) {
				case "enabled":
					return { ...prev, enabled: checked };
				case "topic":
					return { ...prev, topic: value };
				default:
					return prev;
			}
		});
	};

	const handleCopySubscribeUrl = async (): Promise<void> => {
		if (ntfySubscribeUrl == null) {
			return;
		}

		if (
			typeof navigator === "undefined" ||
			navigator.clipboard?.writeText == null
		) {
			setCopySubscribeUrlFeedback("Copy not supported in this browser");
			return;
		}

		try {
			await navigator.clipboard.writeText(ntfySubscribeUrl);
			setCopySubscribeUrlFeedback("Copied!");
		} catch {
			setCopySubscribeUrlFeedback("Could not copy URL");
		}

		setTimeout(() => {
			setCopySubscribeUrlFeedback(null);
		}, 2500);
	};

	const handleNotificationSettingsSave = async (
		e: FormEvent<HTMLFormElement>,
	): Promise<void> => {
		e.preventDefault();

		const token = getAuthTokenOrRedirect();
		if (token == null) {
			return;
		}

		setNotificationSaving(true);
		setNotificationError(null);
		setNotificationSuccess(null);

		try {
			const payload: Record<string, unknown> = {
				enabled: notificationForm.enabled,
				topic: notificationForm.topic.trim(),
			};

			const response = await fetch("/api/notifications/settings", {
				method: "PUT",
				headers: createAuthHeaders(token, true),
				body: JSON.stringify(payload),
			});

			if (!response.ok) {
				const data: unknown = await response.json().catch(() => null);
				throw new Error(
					getApiErrorMessageFromBody(
						data,
						"Failed to save notification settings",
					),
				);
			}

			const data: unknown = await response.json().catch(() => null);
			applyNotificationSettings(data);
			setNotificationSuccess("Notification settings saved");
		} catch (saveError) {
			setNotificationError(
				getApiErrorMessage(saveError, "Failed to save notification settings"),
			);
		} finally {
			setNotificationSaving(false);
		}
	};

	const handleSendTestNotification = async (): Promise<void> => {
		const token = getAuthTokenOrRedirect();
		if (token == null) {
			return;
		}

		setNotificationTesting(true);
		setNotificationError(null);
		setNotificationSuccess(null);

		try {
			const response = await fetch("/api/notifications/test", {
				method: "POST",
				headers: createAuthHeaders(token),
			});

			const body: unknown = await response.json().catch(() => null);
			if (!response.ok) {
				throw new Error(
					getApiErrorMessageFromBody(body, "Failed to send test notification"),
				);
			}

			setNotificationSuccess(
				getApiErrorMessageFromBody(body, "Test notification sent"),
			);
		} catch (testError) {
			setNotificationError(
				getApiErrorMessage(testError, "Failed to send test notification"),
			);
		} finally {
			setNotificationTesting(false);
		}
	};

	if (loading) {
		return <div>Loading...</div>;
	}

	if (error) {
		return <div>Error: {error}</div>;
	}

	return (
		<div className="max-w-2xl mx-auto p-4">
			<h2 className="text-2xl font-bold mb-6 text-gray-900 dark:text-white">
				My Account
			</h2>

			<AccountInfoSection
				userName={userName}
				email={email}
				userNameId={userNameId}
				emailId={emailId}
			>
				<PasswordSection
					showPasswordForm={showPasswordForm}
					passwordChangeSuccess={passwordChangeSuccess}
					passwordChangeError={passwordChangeError}
					currentPasswordId={currentPasswordId}
					newPasswordId={newPasswordId}
					confirmPasswordId={confirmPasswordId}
					passwordForm={passwordForm}
					passwordFieldErrors={passwordFieldErrors}
					isChangingPassword={isChangingPassword}
					onOpenPasswordForm={handleOpenPasswordForm}
					onPasswordChange={handlePasswordChange}
					onPasswordInputChange={handlePasswordInputChange}
					onCancelPasswordChange={handleCancelPasswordChange}
				/>
			</AccountInfoSection>

			<NotificationSettingsSection
				notificationError={notificationError}
				notificationSuccess={notificationSuccess}
				notificationLoading={notificationLoading}
				notificationSaving={notificationSaving}
				notificationTesting={notificationTesting}
				notificationForm={notificationForm}
				reminderMinutesBeforeDue={reminderMinutesBeforeDue}
				publicNtfyBaseUrl={publicNtfyBaseUrl}
				ntfyTopicPreview={ntfyTopicPreview}
				ntfyTopicPrefixDisplay={ntfyTopicPrefixDisplay}
				ntfySubscribeUrl={ntfySubscribeUrl}
				copySubscribeUrlFeedback={copySubscribeUrlFeedback}
				ntfyTopicId={ntfyTopicId}
				onNotificationInputChange={handleNotificationInputChange}
				onNotificationSettingsSave={handleNotificationSettingsSave}
				onSendTestNotification={handleSendTestNotification}
				onCopySubscribeUrl={handleCopySubscribeUrl}
			/>

			<button
				type="button"
				onClick={() => {
					navigate("/tasks");
				}}
				className="text-white bg-gray-700 hover:bg-gray-800 font-medium rounded-lg text-sm px-5 py-2.5 dark:bg-gray-600 dark:hover:bg-gray-500"
			>
				Back to Tasks
			</button>
		</div>
	);
};

export default MyAccount;
