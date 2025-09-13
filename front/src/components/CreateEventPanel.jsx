import { useState } from "react";
import axios from "axios";

const api = axios.create({
    headers: { "Content-Type": "application/json" }
});

export default function CreateEventPanel({ onCreated }) {
    const [v, setV] = useState({ name: "", ticketsCount: "", eventType: "" });
    const [busy, setBusy] = useState(false);
    const [msg, setMsg] = useState(null);

    const set = k => e => setV(s => ({ ...s, [k]: e.target.value }));

    const save = async () => {
        setMsg(null);
        if (!v.name.trim()) { setMsg({ ok:false, text:"Название обязательно" }); return; }
        if (!(Number(v.ticketsCount) > 0)) { setMsg({ ok:false, text:"ticketsCount > 0" }); return; }

        setBusy(true);
        try {
            const payload = {
                name: v.name.trim(),
                ticketsCount: Number(v.ticketsCount),
                eventType: v.eventType || undefined
            };
            const res = await api.post("/add_event", payload);
            setMsg({ ok:true, text:`Event id=${res.data?.id ?? "—"} создан` });
            setV({ name:"", ticketsCount:"", eventType:"" });
            onCreated?.();
        } catch (e) {
            setMsg({ ok:false, text:`Ошибка: ${e.response?.status || ""} ${e.response?.data || e.message}` });
        } finally { setBusy(false); }
    };

    const EVENT_TYPES = ["CONCERT","FOOTBALL","BASEBALL","BASKETBALL","OPERA"];

        return (
            <div style={{ display:"grid", gridTemplateColumns:"1fr 2fr", gap:12, maxWidth:620 }}>
                <label>Название *</label>
                <input value={v.name} onChange={set("name")} />

                <label>ticketsCount *</label>
                <input type="number" min="1" value={v.ticketsCount} onChange={set("ticketsCount")} />

                <label>eventType</label>
                <select value={v.eventType} onChange={set("eventType")}>
                    <option value="">— не задан —</option>
                    {EVENT_TYPES.map(t => <option key={t} value={t}>{t}</option>)}
                </select>

                <div style={{ gridColumn:"1 / -1" }}>
                    <button onClick={save} disabled={busy}>{busy ? "Сохраняю..." : "Создать Event"}</button>
                    {msg && <span style={{ color: msg.ok ? "green" : "crimson", marginLeft:12 }}>{msg.text}</span>}
                </div>
            </div>
        );

}