import type { ReactElement } from "react";

import type { PasswordSectionProps } from "./types";

type FieldErrorListProps = {
  errors: string[];
};

const FieldErrorList = ({
  errors,
}: FieldErrorListProps): ReactElement | null => {
  if (errors.length === 0) {
    return null;
  }

  return (
    <div className="mt-1 text-sm text-red-600 dark:text-red-400">
      {errors.map((fieldError) => (
        <div key={fieldError}>{fieldError}</div>
      ))}
    </div>
  );
};

const PasswordSection = ({
  showPasswordForm,
  passwordChangeSuccess,
  passwordChangeError,
  currentPasswordId,
  newPasswordId,
  confirmPasswordId,
  passwordForm,
  passwordFieldErrors,
  isChangingPassword,
  onOpenPasswordForm,
  onPasswordChange,
  onPasswordInputChange,
  onCancelPasswordChange,
}: PasswordSectionProps): ReactElement => (
  <div className="pt-4 mt-4 border-t border-gray-300 dark:border-gray-600">
    {passwordChangeSuccess && (
      <div className="mb-4 p-3 text-sm text-green-800 border border-green-300 rounded-lg bg-green-50 dark:bg-gray-800 dark:text-green-400 dark:border-green-800">
        {passwordChangeSuccess}
      </div>
    )}

    {passwordChangeError && (
      <div className="mb-4 p-3 text-sm text-red-800 border border-red-300 rounded-lg bg-red-50 dark:bg-gray-800 dark:text-red-400 dark:border-red-800">
        {passwordChangeError}
      </div>
    )}

    {!showPasswordForm ? (
      <button
        type="button"
        onClick={onOpenPasswordForm}
        className="text-white bg-blue-700 hover:bg-blue-800 font-medium rounded-lg text-sm px-5 py-2.5"
      >
        Change Password
      </button>
    ) : (
      <form onSubmit={onPasswordChange} method="POST">
        <div className="mb-4">
          <label
            htmlFor={currentPasswordId}
            className="block mb-2 text-sm font-medium text-gray-700 dark:text-white"
          >
            Current Password
          </label>
          <input
            type="password"
            id={currentPasswordId}
            name="currentPassword"
            value={passwordForm.currentPassword}
            onChange={onPasswordInputChange}
            disabled={isChangingPassword}
            className="bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg focus:ring-blue-500 focus:border-blue-500 block w-full p-2.5 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500"
          />
          <FieldErrorList errors={passwordFieldErrors.currentPassword} />
        </div>

        <div className="mb-4">
          <label
            htmlFor={newPasswordId}
            className="block mb-2 text-sm font-medium text-gray-700 dark:text-white"
          >
            New Password
          </label>
          <input
            type="password"
            id={newPasswordId}
            name="newPassword"
            value={passwordForm.newPassword}
            onChange={onPasswordInputChange}
            disabled={isChangingPassword}
            className="bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg focus:ring-blue-500 focus:border-blue-500 block w-full p-2.5 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500"
          />
          <FieldErrorList errors={passwordFieldErrors.newPassword} />
        </div>

        <div className="mb-4">
          <label
            htmlFor={confirmPasswordId}
            className="block mb-2 text-sm font-medium text-gray-700 dark:text-white"
          >
            Confirm New Password
          </label>
          <input
            type="password"
            id={confirmPasswordId}
            name="confirmPassword"
            value={passwordForm.confirmPassword}
            onChange={onPasswordInputChange}
            disabled={isChangingPassword}
            className="bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg focus:ring-blue-500 focus:border-blue-500 block w-full p-2.5 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500"
          />
          <FieldErrorList errors={passwordFieldErrors.confirmPassword} />
        </div>

        <div className="flex gap-2">
          <button
            type="submit"
            disabled={isChangingPassword}
            className="text-white bg-blue-700 hover:bg-blue-800 font-medium rounded-lg text-sm px-5 py-2.5 disabled:opacity-50"
          >
            {isChangingPassword ? "Changing..." : "Change Password"}
          </button>

          <button
            type="button"
            onClick={onCancelPasswordChange}
            disabled={isChangingPassword}
            className="text-gray-700 bg-gray-200 hover:bg-gray-300 font-medium rounded-lg text-sm px-5 py-2.5 dark:bg-gray-600 dark:text-white dark:hover:bg-gray-500 disabled:opacity-50"
          >
            Cancel
          </button>
        </div>
      </form>
    )}
  </div>
);

export default PasswordSection;
