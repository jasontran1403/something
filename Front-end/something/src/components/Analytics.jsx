import { Menu, Transition } from "@headlessui/react";
import Axios from "axios";
import { useEffect, useState } from "react";

import Image from "../components/Imgage";
import config from "../config";
import { formatToCurrency, toast } from "../helpers";
import env from "../helpers/env";

const Analytics = ({
    cashBalance = 0,
    commissionBalance = 0,
    rank = 0,
    personalSale = 0,
    leftGroupSale = 0,
    rightGroupSale = 0,
    leftAccumulateSale = 0,
    rightAccumulateSale = 0
}) => {
    const currentUsername = config.AUTH.DRIVER.getItem("username");
    if (typeof cashBalance === "string") {
        cashBalance = 0;
    }

    if (typeof commissionBalance === "string") {
        commissionBalance = 0;
    }

    const [expenses, setExpenses] = useState({});
    const [investments, setInvestments] = useState([]);

    const packageName = [
        {
            name: "Default"
        },
        {
            name: "Standard"
        },
        {
            name: "Starter"
        },
        {
            name: "VIP01"
        },
        {
            name: "VIP02"
        },
        {
            name: "VIP03"
        },
        {
            name: "Business"
        },
        {
            name: "X-Angel"
        },
        {
            name: "Big Share Holder"
        },
        {
            name: "Co-Owner"
        }
    ];

    useEffect(() => {
        let config = {
            method: "get",
            url: `${env}/api/history/runningInvestment/${currentUsername}`
        };

        Axios(config).then(response => {
            setInvestments(response.data);
            if (response.data.length > 0) {
                setExpenses({
                    investmentName: response.data[0].packageId,
                    startAt: response.data[0].time.substring(9, response.data[0].time.length),
                    endAt: countDateEnd(response.data[0].time),
                    percentage: Math.floor((response.data[0].count / 300) * 100),
                    claimable: response.data[0].claimable
                });
            } else {
                setExpenses({});
            }
        });
    }, [currentUsername]);

    const countDateEnd = date => {
        const newdate =
            date.substring(15, date.length) +
            "-" +
            date.substring(12, 14) +
            "-" +
            date.substring(9, 11);
        var someDate = new Date(newdate);
        someDate.setDate(someDate.getDate() + 300);
        var dateFormated = someDate.toISOString().substr(0, 10);
        return (
            dateFormated.substring(8, dateFormated.length) +
            "/" +
            dateFormated.substring(5, 7) +
            "/" +
            dateFormated.substring(0, 4)
        );
    };

    const handleSubmit = () => {
        let config = {
            method: "get",
            url: `${env}/api/investment/withdrawCapital/cac7ac4b0472`
        };

        Axios(config)
            .then(() => {
                toast("success", "Withdraw capital successful");
            })
            .catch(error => {
                console.log(error);
            });
    };

    return (
        <div className="grid grid-cols-6 gap-8" style={{ height: "300px" }}>
            <div className="col-span-6 px-4 py-3 bg-white border rounded-md shadow-xs lg:col-span-2">
                <div className="flex justify-center">
                    <Menu>
                        <div className="relative">
                            {investments.length > 0 ? (
                                <>
                                    <Menu.Button className="flex items-center justify-center w-12 h-12 text-2xl text-gray-400">
                                        <svg
                                            xmlns="http://www.w3.org/2000/svg"
                                            className="w-6 h-6"
                                            fill="none"
                                            viewBox="0 0 24 24"
                                            stroke="currentColor"
                                        >
                                            <path
                                                strokeLinecap="round"
                                                strokeLinejoin="round"
                                                strokeWidth="2"
                                                d="M4 6h16M4 12h16M4 18h16"
                                            />
                                        </svg>
                                    </Menu.Button>
                                </>
                            ) : (
                                ""
                            )}

                            <Transition>
                                <Menu.Items className="absolute z-10 mr-3.5 top-0 mt-9 py-1.5 right-0 text-xs text-gray-700 bg-white border rounded-md w-40">
                                    {investments.map((investment, index) => {
                                        return (
                                            <Menu.Item key={index}>
                                                <a
                                                    className="inline-block w-full px-5 py-2 hover:bg-gray-100 hover:text-blue-500"
                                                    onClick={() => {
                                                        setExpenses({
                                                            investmentName: investment.packageId,
                                                            startAt: investment.time.substring(
                                                                9,
                                                                investment.time.length
                                                            ),
                                                            endAt: countDateEnd(investment.time),
                                                            percentage: Math.floor(
                                                                (investment.count / 300) * 100
                                                            ),
                                                            claimable: investment.claimable
                                                        });
                                                    }}
                                                >
                                                    G??i {packageName[investment.packageId].name}
                                                </a>
                                            </Menu.Item>
                                        );
                                    })}
                                </Menu.Items>
                            </Transition>
                        </div>
                    </Menu>
                </div>

                {investments.length > 0 ? (
                    <>
                        <div className="flex flex-col items-center justify-between">
                            <p className="text-4xl">
                                G??i {packageName[expenses.investmentName].name}
                            </p>
                        </div>

                        <div className="flex justify-between col-span-1 mt-3">
                            <p className="text-2xl font-light text-orange-500 transition-all duration-300">
                                Start At: {expenses.startAt}
                            </p>

                            <p className="text-2xl font-light text-orange-500 transition-all duration-300">
                                End At: {expenses.endAt}
                            </p>
                        </div>

                        <div className="flex items-center mt-5 space-x-5">
                            <div className="w-full px-2 py-1 bg-gray-200 rounded-full">
                                <div
                                    className="transition-all duration-300 flex items-center justify-end h-3 rounded-full bg-gradient-to-r from-blue-500 to-blue-900"
                                    style={{ width: `${expenses.percentage}%` }}
                                >
                                    <div className="w-2 h-2 mr-1 bg-white rounded-full" />
                                </div>
                            </div>

                            <p className="font-medium text-orange-500">{expenses.percentage}%</p>
                        </div>

                        <div className="flex items-end space-x-96">
                            <p className="text-2xl font-light text-orange-500 transition-all duration-300">
                                Claimable: {formatToCurrency(expenses.claimable)}
                            </p>
                        </div>

                        <div className="flex items-center justify-center">
                            {expenses.claimable > 0 ? (
                                <div className="px-2 py-1 font-semibold text-black-300 bg-emerald-400 rounded">
                                    <button className="place-items-center" onClick={handleSubmit}>
                                        Withdraw Capital
                                    </button>
                                </div>
                            ) : (
                                <div className="px-2 py-1 font-semibold text-white-400 bg-neutral-400 rounded">
                                    <button className="place-items-center" disabled>
                                        Withdraw Capital
                                    </button>
                                </div>
                            )}
                        </div>
                    </>
                ) : (
                    <>
                        <div className="flex mt-24 justify-center">
                            <p className="text-4xl font-light text-orange-500 justifty-center transition-all duration-300">
                                Ch??a tham gia mua g??i
                            </p>
                        </div>
                    </>
                )}
            </div>

            <div className="col-span-6 px-4 py-3 bg-white border rounded-md shadow-xs md:col-span-3 lg:col-span-2">
                <div className="flex flex-col items-center justify-between">
                    <p className="text-lg">Doanh s??? c?? nh??n</p>
                    <p className="text-lg self-center">{formatToCurrency(personalSale)}</p>
                </div>
                <div className="flex justify-between col-span-1 mt-3">
                    <div className="flex flex-col justify-start col-span-1 mt-8">
                        <p className="text-lg">DS Nh??m Tr??i</p>
                        <p className="text-lg self-center">{formatToCurrency(leftGroupSale)}</p>
                    </div>
                    <div className="flex flex-col justify-end col-span-1 mt-8">
                        <p className="text-lg">DS Nh??m Ph???i</p>
                        <p className="text-lg self-center">{formatToCurrency(rightGroupSale)}</p>
                    </div>
                </div>
                <div className="flex justify-between col-span-1 mt-3">
                    <div className="flex flex-col justify-start col-span-1 mt-8">
                        <p className="text-lg">DS T??ch l??y Tr??i</p>
                        <p className="text-lg self-center">
                            {formatToCurrency(leftAccumulateSale)}
                        </p>
                    </div>
                    <div className="flex flex-col justify-end col-span-1 mt-8">
                        <p className="text-lg">DS T??ch l??y Ph???i</p>
                        <p className="text-lg self-center">
                            {formatToCurrency(rightAccumulateSale)}
                        </p>
                    </div>
                </div>
            </div>
            <div className="relative col-span-6 px-4 py-3 overflow-hidden text-white bg-white bg-blue-500 border rounded-md shadow-xs md:col-span-3 lg:col-span-2">
                <div
                    className="absolute z-0 bg-blue-800 rounded-full bg-opacity-70 ml-36"
                    style={{ height: "310px", width: "382px", left: "8%", top: "-45px" }}
                />
                <div className="relative flex justify-between">
                    <h3 className="font-light text-white text-opacity-100">USDT Wallet</h3>
                    <div className="text-right">
                        <p className="text-xl font-light text-white text-opacity-90">
                            {formatToCurrency(cashBalance)}
                        </p>
                    </div>
                </div>
                <div className="relative flex justify-between">
                    <h3 className="font-light text-white text-opacity-100">Commission Wallet</h3>
                    <div className="text-right">
                        <p className="text-xl font-light text-white text-opacity-90">
                            {formatToCurrency(commissionBalance)}
                        </p>
                    </div>
                </div>

                <div className="relative flex justify-center" style={{ height: "313px" }}>
                    <div className="text-right">{rank > 0 && <Image src={rank} />}</div>
                </div>
            </div>
        </div>
    );
};

export default Analytics;
