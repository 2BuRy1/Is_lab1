// src/components/CreateTicketPanel.jsx
import { useEffect, useState } from "react";
import axios from "axios";

const api = axios.create({
    headers: { "Content-Type": "application/json" },
});

const TYPES = ["VIP", "USUAL", "BUDGETARY", "CHEAP"];

const empty = {
    name: "", price: "", type: "", number: "",
    discount: "", comment: "",
    coordX: "", coordY: "",
    personId: "", eventId: "", venueId: ""
};

export default function CreateTicketPanel({ onCreated }) {
    const [v, setV] = useState(empty);
    const [persons, setPersons] = useState([]);
    const [events, setEvents]   = useState([]);
    const [venues, setVenues]   = useState([]);
    const [busy, setBusy]       = useState(false);
    const [msg, setMsg] = useState(null);

    const set = (k) => (e) => setV(s => ({ ...s, [k]: e.target.value }));

    useEffect(() => {
        loadPersons();
        loadEvents();
        loadVenues();
    }, []);

    const parseArray = (data, key) => {
        if (key && Array.isArray(data?.[key])) return data[key];
        if (Array.isArray(data)) return data;
        return [];
    };

    async function loadPersons() {
        try {
            const res = await api.get("/get_persons");
            const arr = parseArray(res.data, "persons");
            setPersons(arr);
        } catch (e) {
            console.warn("persons load failed:", e);
            setMsg({ ok:false, text: `Persons 404/err: ${e.response?.status || e.message}` });
        }
    }

    async function loadEvents() {
        try {
            const res = await api.get("/get_events");
            console.log("get_events response:", res.data);
            const arr = parseArray(res.data, "events");
            setEvents(arr);
        } catch (e) {
            console.warn("events load failed:", e);
            setMsg({ ok:false, text: `Events 404/err: ${e.response?.status || e.message}` });
        }
    }

    async function loadVenues() {
        try {
            const res = await api.get("/get_venues");
            const arr = parseArray(res.data, "venues");
            setVenues(arr);
        } catch (e) {
            console.warn("venues load failed:", e);
            setMsg({ ok:false, text: `Venues 404/err: ${e.response?.status || e.message}` });
        }
    }

    const validate = () => {
        if (!v.name.trim()) return "Название обязательно";
        if (!(Number(v.price) > 0)) return "Цена должна быть > 0";
        if (!v.type) return "Тип обязателен";
        if (!(Number(v.number) > 0)) return "Количество должно быть > 0";
        if (v.discount !== "" && !(Number(v.discount) > 0 && Number(v.discount) <= 100))
            return "Скидка должна быть в (0;100]";
        if (v.coordX === "" || v.coordY === "") return "Координаты X и Y обязательны";
        return null;
    };

    const buildPayload = () => {
        const payload = {
            name: v.name.trim(),
            price: Number(v.price),
            type: v.type,
            number: Number(v.number),
            coordinates: { x: Number(v.coordX), y: Number(v.coordY) },
            comment: v.comment.trim() || undefined,
        };
        if (v.discount !== "") payload.discount = Number(v.discount);
        if (v.personId) payload.person = { id: Number(v.personId) };
        if (v.eventId)  payload.event  = { id: Number(v.eventId) };
        if (v.venueId)  payload.venue  = { id: Number(v.venueId) };
        return payload;
    };

    const save = async () => {
        setMsg(null);
        const err = validate();
        if (err) { setMsg({ ok:false, text:err }); return; }

        setBusy(true);
        try {
            const res = await api.post("/add_ticket", buildPayload());
            setMsg({ ok:true, text:`Создан билет id=${res.data?.id ?? "—"}` });
            setV(empty);
            onCreated?.();
        } catch (e) {
            const text = typeof e.response?.data === "string"
                ? e.response.data : JSON.stringify(e.response?.data || {});
            setMsg({ ok:false, text:`Ошибка: ${e.response?.status || ""} ${text}` });
        } finally {
            setBusy(false);
        }
    };

    return (
        <div style={{ display:"grid", gridTemplateColumns:"1fr 2fr", gap:12, maxWidth:820 }}>
            <label>Название *</label>
            <input value={v.name} onChange={set("name")} />

            <label>Цена *</label>
            <input type="number" step="0.01" min="0.01" value={v.price} onChange={set("price")} />

            <label>Тип *</label>
            <select value={v.type} onChange={set("type")}>
                <option value="">— выберите —</option>
                {TYPES.map(t => <option key={t} value={t}>{t}</option>)}
            </select>

            <label>Количество *</label>
            <input type="number" min="1" value={v.number} onChange={set("number")} />

            <label>Скидка (0–100)</label>
            <input type="number" min="0.01" max="100" step="0.01" value={v.discount} onChange={set("discount")} />

            <label>Комментарий</label>
            <input value={v.comment} onChange={set("comment")} />

            <label>Координата X *</label>
            <input type="number" step="1" value={v.coordX} onChange={set("coordX")} />

            <label>Координата Y *</label>
            <input type="number" step="0.01" value={v.coordY} onChange={set("coordY")} />

            <label>Владелец (Person)</label>
            <select value={v.personId} onChange={set("personId")}>
                <option value="">— не задан —</option>
                {persons.map(p => (
                    <option key={p.id} value={p.id}>
                        {p.passportID || `Person #${p.id}`}
                    </option>
                ))}
            </select>

            <label>Событие (Event)</label>
            <select value={v.eventId} onChange={set("eventId")}>
                <option value="">— не задано —</option>
                {events.map(ev => (
                    <option key={ev.id} value={ev.id}>
                        {ev.name} {ev.ticketsCount != null ? `(${ev.ticketsCount})` : ""}
                    </option>
                ))}
            </select>

            <label>Площадка (Venue)</label>
            <select value={v.venueId} onChange={set("venueId")}>
                <option value="">— не задано —</option>
                {venues.map(ve => (
                    <option key={ve.id} value={ve.id}>
                        {ve.name}
                    </option>
                ))}
            </select>

            <div style={{ gridColumn:"1 / -1", display:"flex", gap:12, alignItems:"center" }}>
                <button onClick={save} disabled={busy}>{busy ? "Сохраняю..." : "Сохранить"}</button>
                {msg && <span style={{ color: msg.ok ? "green" : "crimson" }}>{msg.text}</span>}
            </div>
        </div>
    );
}