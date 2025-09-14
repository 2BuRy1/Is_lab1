import { useState } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";
import Table from "../components/Table";
import "../styles/main.css";

const api = axios.create({ headers: { "Content-Type": "application/json" } });

export default function MainPage() {
    const [reloadKey, setReloadKey] = useState(0);
    const [busy, setBusy] = useState(false);
    const [msg, setMsg] = useState(null);
    const [searchId, setSearchId] = useState("");
    const [found, setFound] = useState(null);

    const navigate = useNavigate();

    const bump = () => setReloadKey((k) => k + 1);

    const onDeleteClick = async () => {
        const idStr = window.prompt("ID билета для удаления:");
        const id = Number(idStr);
        if (!id || Number.isNaN(id)) return;
        if (!window.confirm(`Удалить билет ${id}?`)) return;

        setBusy(true);
        setMsg(null);
        try {
            await api.delete(`/delete_ticket/${id}`);
            setMsg({ ok: true, text: `Билет ${id} удалён` });
            bump();
        } catch (e) {
            setMsg({
                ok: false,
                text: ` ${e.response?.data || e.message}`,
            });
        } finally {
            setBusy(false);
        }
    };

    const onEditPageClick = () => {
        const idStr = window.prompt("ID билета для редактирования на отдельной странице:");
        const id = Number(idStr);
        if (!id || Number.isNaN(id)) return;
        navigate(`/tickets/${id}/edit`);
    };

    const onFunctrions = () => {
        navigate("/functions");
    };

    const onSearchById = async () => {
        if (!searchId.trim()) return;
        const id = Number(searchId);
        if (!id || Number.isNaN(id)) return;

        setBusy(true);
        setMsg(null);
        try {
            const res = await api.get(`/get_ticket/${id}`);
            setFound(res.data);
        } catch (e) {
            setFound(null);
            setMsg({
                ok: false,
                text: `Ошибка. Объект не найден`,
            });
        } finally {
            setBusy(false);
        }
    };

    const clearSearch = () => {
        setSearchId("");
        setFound(null);
    };

    return (
        <div className="page">
            <div className="toolbar">
                <button className="btn" onClick={() => navigate("/tickets/new")} disabled={busy}>
                    Создать билет
                </button>
                <button className="btn" onClick={onEditPageClick} disabled={busy}>
                    Редактировать по ID
                </button>
                <button className="btn btn-danger" onClick={onDeleteClick} disabled={busy}>
                    Удалить по ID
                </button>
                <button className="btn" onClick={onFunctrions} disabled={busy}>
                    Дополнительные функции
                </button>

                <input
                    className="search-input"
                    placeholder="ID билета"
                    value={searchId}
                    onChange={(e) => setSearchId(e.target.value)}
                />
                <button className="btn" onClick={onSearchById} disabled={busy}>
                    Найти
                </button>
                <button className="btn btn-secondary" onClick={clearSearch}>
                    Очистить
                </button>

                {msg && (
                    <span className={msg.ok ? "status-ok" : "status-err"}>{msg.text}</span>
                )}
            </div>

            {found && (
                <div className="details-card">
                    <h3>Билет #{found.id}</h3>
                    <p><b>Название:</b> {found.name}</p>
                    <p><b>Цена:</b> {found.price}</p>
                    <p><b>Тип:</b> {found.type}</p>
                    <p><b>Количество:</b> {found.number}</p>
                    <p><b>Скидка:</b> {found.discount ?? "—"}</p>
                    <p><b>Комментарий:</b> {found.comment ?? "—"}</p>

                    <p><b>Координаты:</b> X={found.coordinates?.x}, Y={found.coordinates?.y}</p>

                    {found.person && (
                        <div className="nested">
                            <h4>Person</h4>
                            <p>ID: {found.person.id}</p>
                            <p>Passport: {found.person.passportID}</p>
                            <p>Weight: {found.person.weight}</p>
                            <p>Nationality: {found.person.nationality}</p>
                        </div>
                    )}

                    {found.event && (
                        <div className="nested">
                            <h4>Event</h4>
                            <p>ID: {found.event.id}</p>
                            <p>Название: {found.event.name}</p>
                            <p>Дата: {found.event.date}</p>
                        </div>
                    )}

                    {found.venue && (
                        <div className="nested">
                            <h4>Venue</h4>
                            <p>ID: {found.venue.id}</p>
                            <p>Название: {found.venue.name}</p>
                            <p>Вместимость: {found.venue.capacity}</p>
                            <p>Тип: {found.venue.venueType}</p>
                        </div>
                    )}
                </div>
            )}

            <Table key={reloadKey} tableName="mainTable" />
        </div>
    );
}