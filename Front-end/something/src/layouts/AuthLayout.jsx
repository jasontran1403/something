import { HiBeaker } from "react-icons/hi2";
import { Link } from "react-router-dom";

const AuthLayout = ({ children }) => {
    return (
        <div className="flex text-gray-800 p-4 md:py-6 md:px-32 lg:p-0">
            <div className="flex flex-col-reverse lg:flex-row w-screen lg:min-h-screen border shadow-sm rounded-lg overflow-hidden lg:border-none lg:shadow-none lg:rounded-none lg:overflow-auto">
                <div className="flex flex-col justify-between text-white lg:min-h-screen w-full lg:w-7/12 xl:w-3/5 bg-[#111827]">
                    <img className="w-8/12 h-auto" src="/images/auth-5.png" alt="" />

                    <div className="space-y-8 p-9">
                        <Link to="/" className="flex items-center space-x-3">
                            <HiBeaker className="w-9 h-9 md:w-12 md:h-12 text-indigo-600" />
                            <div>
                                <p className="inline text-xl md:text-2xl uppercase font-bold leading-[0.5rem]">
                                    Atom<span className="font-[300]">pay</span>
                                </p>
                                <div className="flex items-center space-x-0.5 leading-[0.5rem]">
                                    <span className="text-[0.62rem] font-bold text-indigo-600 uppercase leading-[0.5rem]">
                                        React
                                    </span>
                                    <hr className="w-5 border-sky-600" />
                                </div>
                            </div>
                        </Link>
                    </div>
                </div>

                <div className="flex flex-col justify-center lg:min-h-screen p-6 md:p-10 lg:p-8 xl:p-10 w-full lg:w-5/12 xl:w-2/5">
                    {children}
                </div>
            </div>
        </div>
    );
};

export default AuthLayout;
