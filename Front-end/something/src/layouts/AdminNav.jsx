import { Menu, Transition } from "@headlessui/react";
import { HiBeaker } from "react-icons/hi2";
import { Link, useNavigate } from "react-router-dom";

import { logout } from "../helpers";
import "../assets/NavMenu.css";

const AdminNav = () => {
    const navigate = useNavigate();

    const handleClick = e => {
        e.preventDefault();
        logout(navigate);
    };

    return (
        <div className="flex items-center justify-between py-2 pl-6 pr-4 text-white bg-gray-900">
            <div className="flex items-center w-1/2 space-x-16">
                <Link to="/dashboard" className="flex items-center space-x-3">
                    <HiBeaker className="w-9 h-9 md:w-12 md:h-12 text-indigo-600" />
                </Link>
            </div>

            <div className="flex items-center space-x-5">
                <Menu>
                    <div className="relative">
                        <Link
                            to={"/dashboard"}
                            className="flex items-center space-x-3 px-4 py-2.5 text-sm hover:text-purple-500"
                        >
                            <h3>Dashboard</h3>
                        </Link>
                    </div>
                    <div className="relative">
                        <Link
                            to={"/invest"}
                            className="flex items-center space-x-3 px-4 py-2.5 text-sm hover:text-purple-500"
                        >
                            <h3>Investment</h3>
                        </Link>
                    </div>
                    {/* <div className="relative">
                        <Link
                            to={"/investment"}
                            className="flex items-center space-x-3 px-4 py-2.5 text-sm hover:text-purple-500"
                        >
                            <h3>Investment</h3>
                        </Link>
                    </div> */}
                    <div className="relative">
                        <Link
                            to={"/treeview"}
                            className="flex items-center space-x-3 px-4 py-2.5 text-sm hover:text-purple-500"
                        >
                            <h3>Tree View</h3>
                        </Link>
                    </div>
                    <div className="relative">
                        <div className="flex items-center space-x-3 px-4 py-2.5 text-sm hover:text-purple-500 div">
                            <ul className="menu">
                                <li>
                                    <h3>Wallet</h3>
                                    <ul>
                                        <li>
                                            <Link
                                                to={"/deposit"}
                                                className="flex items-center space-x-3 px-4 py-2.5 text-sm hover:text-purple-500"
                                            >
                                                <h3>Deposit</h3>
                                            </Link>
                                        </li>
                                        <li>
                                            <Link
                                                to={"/swap"}
                                                className="flex items-center space-x-3 px-4 py-2.5 text-sm hover:text-purple-500"
                                            >
                                                <h3>Swap</h3>
                                            </Link>
                                        </li>
                                        <li>
                                            <Link
                                                to={"/transfer"}
                                                className="flex items-center space-x-3 px-4 py-2.5 text-sm hover:text-purple-500"
                                            >
                                                <h3>Transfer</h3>
                                            </Link>
                                        </li>
                                        <li>
                                            <Link
                                                to={"/withdraw"}
                                                className="flex items-center space-x-3 px-4 py-2.5 text-sm hover:text-purple-500"
                                            >
                                                <h3>Withdraw</h3>
                                            </Link>
                                        </li>
                                    </ul>
                                </li>
                            </ul>
                        </div>
                    </div>
                </Menu>

                <Menu>
                    <div className="relative">
                        <Menu.Button className="focus:outline-none">
                            <img className="w-8 h-8 rounded-md" src="/images/avatar.png" alt="" />
                        </Menu.Button>

                        <Transition
                            enter="transition duration-100 ease-out"
                            enterFrom="transform scale-95 opacity-0"
                            enterTo="transform scale-100 opacity-100"
                            leave="transition duration-75 ease-out"
                            leaveFrom="transform scale-100 opacity-100"
                            leaveTo="transform scale-95 opacity-0"
                        >
                            <Menu.Items>
                                <div className="absolute right-0 z-10 w-48 px-2 py-1 mt-1 text-gray-600 bg-white border rounded-md shadow">
                                    <Menu.Item>
                                        <Link
                                            to={"/profile"}
                                            className="flex items-center space-x-3 px-3 py-2.5 text-sm hover:text-purple-500"
                                        >
                                            <span>Profile</span>
                                        </Link>
                                    </Menu.Item>
                                    <hr />

                                    <Menu.Item>
                                        <Link
                                            to={"/authenticator"}
                                            className="flex items-center space-x-3 px-3 py-2.5 text-sm hover:text-purple-500"
                                        >
                                            <span>2FA Setup</span>
                                        </Link>
                                    </Menu.Item>
                                    <hr />

                                    <Menu.Item>
                                        <a
                                            href={"/logout"}
                                            className="flex items-center space-x-3 px-3 py-2.5 text-sm hover:text-purple-500"
                                            onClick={handleClick}
                                        >
                                            <span>Logout</span>
                                        </a>
                                    </Menu.Item>
                                </div>
                            </Menu.Items>
                        </Transition>
                    </div>
                </Menu>
            </div>
        </div>
    );
};

export default AdminNav;
