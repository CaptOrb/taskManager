import type { ReactElement } from "react";

import type { NotificationSettingsSectionProps } from "./types";

const NotificationSettingsSection = ({
	notificationError,
	notificationSuccess,
	notificationLoading,
	notificationSaving,
	notificationTesting,
	topicSuggestionLoading,
	notificationForm,
	reminderMinutesBeforeDue,
	publicNtfyBaseUrl,
	ntfyTopicPreview,
	ntfyTopicPrefixDisplay,
	ntfySubscribeUrl,
	copySubscribeUrlFeedback,
	ntfyTopicId,
	onNotificationInputChange,
	onNotificationSettingsSave,
	onSendTestNotification,
	onCopySubscribeUrl,
	onGenerateTopic,
}: NotificationSettingsSectionProps): ReactElement => (
	<div className="mb-4 p-4 border border-gray-300 rounded-lg bg-gray-50 dark:bg-gray-700 dark:border-gray-600">
		<h3 className="text-lg font-medium text-gray-900 dark:text-white mb-2">
			ntfy Notifications
		</h3>
		<p className="text-sm text-gray-600 dark:text-gray-300 mb-4">
			Send due-soon reminders through ntfy ({reminderMinutesBeforeDue} minutes
			before due date).
		</p>

		<div className="mb-4 p-3 text-sm text-blue-800 border border-blue-300 rounded-lg bg-blue-50 dark:bg-gray-800 dark:text-blue-300 dark:border-blue-700">
			<p className="font-medium mb-2">Quick setup</p>
			<ol className="list-decimal list-inside space-y-1.5">
				<li>
					Install the ntfy app ({" "}
					<a
						href="https://ntfy.sh"
						target="_blank"
						rel="noreferrer"
						className="underline hover:text-blue-900 dark:hover:text-blue-200"
					>
						download links
					</a>
					)
				</li>
				<li>Choose a topic name below and click Save ↓</li>
				<li>
					In the ntfy app, subscribe to:
					<div className="mt-1 ml-4 p-2 bg-white dark:bg-gray-900 rounded font-mono text-xs">
						<div>
							Server: <span className="font-semibold">{publicNtfyBaseUrl}</span>
						</div>
						<div className="flex items-center gap-2 flex-wrap">
							<span>
								Topic: <span className="font-semibold">{ntfyTopicPreview}</span>
							</span>
							{ntfySubscribeUrl && (
								<button
									type="button"
									onClick={() => {
										void onCopySubscribeUrl();
									}}
									className="text-blue-700 hover:text-blue-800 dark:text-blue-400 dark:hover:text-blue-300 underline text-xs"
								>
									Copy URL
								</button>
							)}
							{copySubscribeUrlFeedback && (
								<span className="text-green-700 dark:text-green-400 text-xs">
									{copySubscribeUrlFeedback}
								</span>
							)}
						</div>
					</div>
				</li>
			</ol>
		</div>

		{notificationError && (
			<div className="mb-4 p-3 text-sm text-red-800 border border-red-300 rounded-lg bg-red-50 dark:bg-gray-800 dark:text-red-400 dark:border-red-800">
				{notificationError}
			</div>
		)}

		{notificationSuccess && (
			<div className="mb-4 p-3 text-sm text-green-800 border border-green-300 rounded-lg bg-green-50 dark:bg-gray-800 dark:text-green-400 dark:border-green-800">
				{notificationSuccess}
			</div>
		)}

		{notificationLoading ? (
			<p className="text-sm text-gray-600 dark:text-gray-300">
				Loading notification settings...
			</p>
		) : (
			<form onSubmit={onNotificationSettingsSave} method="POST">
				<label className="inline-flex items-center mb-4 cursor-pointer">
					<input
						type="checkbox"
						name="enabled"
						checked={notificationForm.enabled}
						onChange={onNotificationInputChange}
						disabled={notificationSaving}
						className="mr-2 w-4 h-4 text-blue-600 bg-gray-100 border-gray-300 rounded focus:ring-blue-500 dark:focus:ring-blue-600 dark:ring-offset-gray-800 focus:ring-2 dark:bg-gray-700 dark:border-gray-600"
					/>
					<span className="text-sm font-medium text-gray-700 dark:text-white">
						Enable due-soon notifications
					</span>
				</label>

				<label
					htmlFor={ntfyTopicId}
					className="block text-sm font-medium text-gray-700 dark:text-white mb-1"
				>
					Topic Name
					<span className="ml-2 text-xs font-normal text-gray-500 dark:text-gray-400">
						(auto-prefixed with{" "}
						<span className="font-mono">{ntfyTopicPrefixDisplay}</span>)
					</span>
				</label>
				<div className="flex flex-col sm:flex-row gap-2 mb-2">
					<input
						type="text"
						id={ntfyTopicId}
						name="topic"
						value={notificationForm.topic}
						onChange={onNotificationInputChange}
						placeholder="my-tasks-2025"
						autoComplete="off"
						disabled={notificationSaving}
						className="bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg focus:ring-blue-500 focus:border-blue-500 block w-full p-2.5 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500"
					/>
					<button
						type="button"
						onClick={() => {
							onGenerateTopic();
						}}
						disabled={notificationSaving || topicSuggestionLoading}
						className="text-blue-700 border border-blue-700 hover:bg-blue-50 font-medium rounded-lg text-sm px-4 py-2.5 disabled:opacity-50 dark:text-blue-300 dark:border-blue-300 dark:hover:bg-gray-800"
					>
						{topicSuggestionLoading ? "Generating..." : "Generate random topic"}
					</button>
				</div>
				<p className="text-xs text-gray-600 dark:text-gray-300 mb-3 break-all">
					Full topic:{" "}
					<span className="font-mono font-semibold">{ntfyTopicPreview}</span>
				</p>

				<div className="flex flex-col sm:flex-row gap-2">
					<button
						type="submit"
						disabled={notificationSaving}
						className="text-white bg-blue-700 hover:bg-blue-800 font-medium rounded-lg text-sm px-5 py-2.5 disabled:opacity-50"
					>
						{notificationSaving ? "Saving..." : "Save Notification Settings"}
					</button>

					<button
						type="button"
						onClick={() => {
							void onSendTestNotification();
						}}
						disabled={
							notificationTesting ||
							notificationSaving ||
							notificationForm.topic.trim().length === 0
						}
						className="text-white bg-green-700 hover:bg-green-800 font-medium rounded-lg text-sm px-5 py-2.5 disabled:opacity-50"
					>
						{notificationTesting ? "Sending..." : "Send Test Notification"}
					</button>
				</div>
			</form>
		)}
	</div>
);

export default NotificationSettingsSection;
