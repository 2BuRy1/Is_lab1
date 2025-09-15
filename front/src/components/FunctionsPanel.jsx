import "../styles/function.css";
import { useState } from "react";
import { Link } from "react-router-dom";
import {api} from "./api";


const ENDPOINTS = {
    deleteByComment: "/delete_by_comment",
    minEventTicket:  "/min_event_ticket",
    countCommentLess:"/count_comment_less",
    sellTicket:      "/sell_ticket",
    cloneVip:        "/clone_vip",
};

export default function FunctionsPanel() {
    const [msg, setMsg] = useState(null);
    const show = (ok, text) => setMsg({ ok, text });

    const [commentEq, setCommentEq] = useState("");
    const [commentLt, setCommentLt] = useState("");
    const [sell, setSell] = useState({ ticketId: "", personId: "", amount: "" });
    const [cloneId, setCloneId] = useState("");

    const [minTicket, setMinTicket] = useState(null);
    const [countLt, setCountLt]     = useState(null);
    const [cloneResult, setCloneResult] = useState(null);

    const onDeleteByComment = async () => {
        try {
            await api.delete(ENDPOINTS.deleteByComment, { params: { commentEq } });
            show(true, "Удалено");
            setCommentEq("");
        } catch (e) {
            show(false, e.response?.data || e.message);
        }
    };

    const onFetchMinEvent = async () => {
        try {
            const res = await api.get(ENDPOINTS.minEventTicket);
            const t = res.data?.ticket ?? res.data;
            setMinTicket(t || null);
            setMsg(null);
        } catch (e) {
            show(false, e.response?.data || e.message);
            setMinTicket(null);
        }
    };

    const onCountLess = async () => {
        try {
            const res = await api.get(ENDPOINTS.countCommentLess, { params: { comment: commentLt } });
            setCountLt(res.data?.count ?? res.data);
            setMsg(null);
        } catch (e) {
            show(false, e.response?.data || e.message);
        }
    };

    const onSell = async () => {
        try {
            await api.post(ENDPOINTS.sellTicket, {
                ticketId: Number(sell.ticketId),
                personId: Number(sell.personId),
                amount: Number(sell.amount),
            });
            show(true, "Продано");
            setSell({ ticketId: "", personId: "", amount: "" });
        } catch (e) {
            show(false, e.response?.data || e.message);
        }
    };

    const onClone = async () => {
        try {
            const res = await api.post(ENDPOINTS.cloneVip, { ticketId: Number(cloneId) });
            setCloneResult(res.data);
            show(true, "Копия создана");
            setCloneId("");
        } catch (e) {
            show(false, e.response?.data || e.message);
        }
    };

    const renderMinTicket = (t) => {
        if (!t) return null;
        const ev = t.event ?? t;
        return (
            <div className="result-card">
                <div className="title">Билет с минимальным Event</div>
                <div className="kv">
                    <span className="k">Ticket ID</span><span className="v">{t.id}</span>
                    {t.name && (<><span className="k">Название</span><span className="v">{t.name}</span></>)}
                    {t.price != null && (<><span className="k">Цена</span><span className="v">{t.price}</span></>)}
                    {ev.id && (<><span className="k">Event ID</span><span className="v">{ev.id}</span></>)}
                    {ev.name && (<><span className="k">Event name</span><span className="v">{ev.name}</span></>)}
                    {ev.eventType && (<><span className="k">Event type</span><span className="v">{ev.eventType}</span></>)}
                </div>
            </div>
        );
    };

    const renderCountResult = () => {
        if (countLt == null) return null;
        return (
            <div className="result-card">
                <div className="title">Результат подсчёта</div>
                <div className="kv">
                    <span className="k">Количество объектов</span>
                    <span className="v">{countLt}</span>
                </div>
            </div>
        );
    };

    return (
        <div className="funcs-screen">
            <div className="funcs-bar">
                <div className="func-row">
                    <span>Удалить по comment:</span>
                    <input value={commentEq} onChange={(e)=>setCommentEq(e.target.value)} />
                    <button className="btn" onClick={onDeleteByComment}>Удалить</button>
                </div>

                <div className="func-row">
                    <span>Min event:</span>
                    <button className="btn" onClick={onFetchMinEvent}>Показать</button>
                </div>
                {renderMinTicket(minTicket)}

                <div className="func-row">
                    <span>Count comment &lt;:</span>
                    <input value={commentLt} onChange={(e)=>setCommentLt(e.target.value)} />
                    <button className="btn" onClick={onCountLess}>Посчитать</button>
                </div>
                {renderCountResult()}

                <div className="func-row">
                    <span>Продать:</span>
                    <input placeholder="ticketId" value={sell.ticketId} onChange={(e)=>setSell({...sell, ticketId:e.target.value})}/>
                    <input placeholder="personId" value={sell.personId} onChange={(e)=>setSell({...sell, personId:e.target.value})}/>
                    <input placeholder="sum" value={sell.amount} onChange={(e)=>setSell({...sell, amount:e.target.value})}/>
                    <button className="btn" onClick={onSell}>OK</button>
                </div>

                <div className="func-row">
                    <span>Clone VIP:</span>
                    <input placeholder="ticketId" value={cloneId} onChange={(e)=>setCloneId(e.target.value)} />
                    <button className="btn" onClick={onClone}>Клонировать</button>
                    {cloneResult && <div className="result-card"><div className="title">Копия создана</div><div>ID: {cloneResult.id}</div></div>}
                </div>
            </div>

            {msg && <div className={`status ${msg.ok ? "ok" : "err"}`}>{msg.text}</div>}

            <div className="back-link">
                <Link to="/" className="btn">← Назад</Link>
            </div>
        </div>
    );
}