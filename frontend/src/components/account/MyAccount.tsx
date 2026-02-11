import { AxiosError } from "axios";
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
import api from "../../utils/api";
import {
	getApiErrorMessage,
	getApiFieldErrorsFromBody,
} from "../../utils/apiError";
import AccountInfoSection from "./AccountInfoSection";
import NotificationSettingsSection from "./NotificationSettingsSection";
import PasswordSection from "./PasswordSection";
import type {
	NotificationForm,
	NotificationSettingsResponse,
	NotificationTopicSuggestionResponse,
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
	const [topicSuggestionLoading, setTopicSuggestionLoading] =
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

	const checkAuthAndRedirect = useCallback((): boolean => {
		const token = localStorage.getItem("token");
		if (!token) {
			navigate("/login");
			return false;
		}
		return true;
	}, [navigate]);

	const fetchTopicSuggestion = useCallback(
		async ({
			replaceExisting,
			silent = false,
		}: {
			replaceExisting: boolean;
			silent?: boolean;
		}): Promise<void> => {
			if (!checkAuthAndRedirect()) {
				return;
			}

			setTopicSuggestionLoading(true);
			if (!silent) {
				setNotificationError(null);
				setNotificationSuccess(null);
				setCopySubscribeUrlFeedback(null);
			}

			try {
				const response = await api.get<NotificationTopicSuggestionResponse>(
					"/notifications/topic-suggestion",
				);
				const data = response.data;

				const suggestedTopic = (data as NotificationTopicSuggestionResponse)
					.topic;
				const suggestion =
					typeof suggestedTopic === "string" ? suggestedTopic.trim() : "";

				if (suggestion.length === 0) {
					throw new Error("Failed to generate a topic suggestion");
				}

				setNotificationForm((prev) => {
					if (!replaceExisting && prev.topic.trim().length > 0) {
						return prev;
					}
					return { ...prev, topic: suggestion };
				});
				setCopySubscribeUrlFeedback(null);
			} catch (suggestionError) {
				if (!silent) {
					setNotificationError(
						getApiErrorMessage(
							suggestionError,
							"Failed to generate a topic suggestion",
						),
					);
				}
			} finally {
				setTopicSuggestionLoading(false);
			}
		},
		[checkAuthAndRedirect],
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
				if (!checkAuthAndRedirect()) {
					return;
				}

				// Fetch user info
				const userResponse = await api.get<{ userName: string; email: string }>(
					"/auth/current-user",
				);
				setUserName(userResponse.data.userName ?? "");
				setEmail(userResponse.data.email ?? "");

				// Fetch notification settings
				try {
					const notificationResponse =
						await api.get<NotificationSettingsResponse>(
							"/notifications/settings",
						);
					applyNotificationSettings(notificationResponse.data);
					const existingTopic =
						typeof notificationResponse.data.topic === "string"
							? notificationResponse.data.topic.trim()
							: "";
					if (existingTopic.length === 0) {
						void fetchTopicSuggestion({ replaceExisting: false, silent: true });
					}
				} catch (notificationError) {
					setNotificationError(
						getApiErrorMessage(
							notificationError,
							"Failed to fetch notification settings",
						),
					);
				}
			} catch (fetchError) {
				setError(getApiErrorMessage(fetchError, "Failed to fetch user"));
			} finally {
				setLoading(false);
				setNotificationLoading(false);
			}
		};

		void fetchCurrentUser();
	}, [applyNotificationSettings, checkAuthAndRedirect, fetchTopicSuggestion]);

	const handlePasswordChange = async (
		e: FormEvent<HTMLFormElement>,
	): Promise<void> => {
		e.preventDefault();

		if (!checkAuthAndRedirect()) {
			return;
		}

		setIsChangingPassword(true);
		setPasswordChangeError(null);
		setPasswordChangeSuccess(null);
		setPasswordFieldErrors(createEmptyPasswordFieldErrors());

		try {
			await api.post("/auth/change-password", {
				currentPassword: passwordForm.currentPassword,
				newPassword: passwordForm.newPassword,
				confirmPassword: passwordForm.confirmPassword,
			});

			setPasswordChangeSuccess("Password changed successfully");
			setPasswordForm({
				currentPassword: "",
				newPassword: "",
				confirmPassword: "",
			});
			setPasswordFieldErrors(createEmptyPasswordFieldErrors());
			setShowPasswordForm(false);
		} catch (changeError) {
			if (changeError instanceof AxiosError && changeError.response?.data) {
				const fieldErrors = mapApiFieldErrorsToPasswordFields(
					getApiFieldErrorsFromBody(changeError.response.data),
				);
				if (Object.values(fieldErrors).some((errors) => errors.length > 0)) {
					setPasswordFieldErrors(fieldErrors);
					return;
				}
			}
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

	const handleGenerateTopic = (): void => {
		void fetchTopicSuggestion({ replaceExisting: true });
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

		if (!checkAuthAndRedirect()) {
			return;
		}

		setNotificationSaving(true);
		setNotificationError(null);
		setNotificationSuccess(null);

		try {
			const response = await api.put<NotificationSettingsResponse>(
				"/notifications/settings",
				{
					enabled: notificationForm.enabled,
					topic: notificationForm.topic.trim(),
				},
			);

			applyNotificationSettings(response.data);
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
		if (!checkAuthAndRedirect()) {
			return;
		}

		setNotificationTesting(true);
		setNotificationError(null);
		setNotificationSuccess(null);

		try {
			const response = await api.post<{ message: string }>(
				"/notifications/test",
			);
			setNotificationSuccess(response.data.message || "Test notification sent");
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
				topicSuggestionLoading={topicSuggestionLoading}
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
				onGenerateTopic={handleGenerateTopic}
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
