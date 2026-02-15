import type { ReactElement } from "react";

const Footer = (): ReactElement => {
  return (
    <footer className="bg-gray-800 text-white py-4 mt-8">
      <div className="max-w-screen-xl mx-auto text-center">
        <p>&copy; {new Date().getFullYear()} All rights reserved.</p>
      </div>
    </footer>
  );
};

export default Footer;
