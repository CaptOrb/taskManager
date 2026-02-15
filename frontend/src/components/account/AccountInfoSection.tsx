import type { ReactElement } from "react";

import type { AccountInfoSectionProps } from "./types";

const AccountInfoSection = ({
  userName,
  email,
  userNameId,
  emailId,
  children,
}: AccountInfoSectionProps): ReactElement => (
  <div className="mb-6 p-4 border border-gray-300 rounded-lg bg-gray-50 dark:bg-gray-700 dark:border-gray-600">
    <h3 className="text-lg font-medium text-gray-900 dark:text-white mb-4">
      Account Information
    </h3>

    <div className="mb-4">
      <label
        htmlFor={userNameId}
        className="block mb-2 text-sm font-medium text-gray-700 dark:text-white"
      >
        Username
      </label>
      <input
        type="text"
        id={userNameId}
        value={userName}
        disabled
        className="bg-gray-100 border border-gray-300 text-gray-700 text-sm rounded-lg block w-full p-2.5 dark:bg-gray-800 dark:border-gray-600 dark:text-gray-300"
      />
    </div>

    <div className="mb-4">
      <label
        htmlFor={emailId}
        className="block mb-2 text-sm font-medium text-gray-700 dark:text-white"
      >
        Email
      </label>
      <input
        type="email"
        id={emailId}
        value={email}
        disabled
        className="bg-gray-100 border border-gray-300 text-gray-700 text-sm rounded-lg block w-full p-2.5 dark:bg-gray-800 dark:border-gray-600 dark:text-gray-300"
      />
    </div>

    {children}
  </div>
);

export default AccountInfoSection;
