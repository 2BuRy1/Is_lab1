import { useState } from "react";
import axios from "axios";

const api = axios.create({
    headers: { "Content-Type": "application/json" }
});

export default function CreateVenuePanel({ onCreated }) {
    const [v, setV] = useState({ name: "", capacity: "", venueType: "" });
    const [busy, setBusy] = useState(false);
    const [msg, setMsg] = useState(null);

    const set = k => e => setV(s => ({ ...s, [k]: e.target.value }));

    const save = async () => {
        setMsg(null);
        if (!v.name.trim()) { setMsg({ ok:false, text:"Название обязательно" }); return; }
        if (v.capacity !== "" && !(Number(v.capacity) > 0)) {
            setMsg({ ok:false, text:"capacity должно быть > 0" }); return;
        }

        setBusy(true);
        try {
            const payload = {
                name: v.name.trim(),
                capacity: v.capacity === "" ? undefined : Number(v.capacity),
                venueType: v.venueType || undefined
            };
            const res = await api.post("/add_venue", payload);
            setMsg({ ok:true, text:`Venue id=${res.data?.id ?? "—"} создана` });
            setV({ name:"", capacity:"", venueType:"" });
            onCreated?.();
        } catch (e) {
            setMsg({ ok:false, text:`Ошибка: ${e.response?.status || ""} ${e.response?.data || e.message}` });
        } finally { setBusy(false); }
    };

    const VENUE_TYPES = ["LOFT","OPEN_AREA","STADIUM"];

        return (
            <div style={{ display:"grid", gridTemplateColumns:"1fr 2fr", gap:12, maxWidth:620 }}>
                <label>Название *</label>
                <input value={v.name} onChange={set("name")} />

                <label>capacity</label>
                <input type="number" min="1" value={v.capacity} onChange={set("capacity")} />

                <label>venueType</label>
                <select value={v.venueType} onChange={set("venueType")}>
                    <option value="">— не задан —</option>
                    {VENUE_TYPES.map(t => <option key={t} value={t}>{t}</option>)}
                </select>

                <div style={{ gridColumn:"1 / -1" }}>
                    <button onClick={save} disabled={busy}>{busy ? "Сохраняю..." : "Создать Venue"}</button>
                    {msg && <span style={{ color: msg.ok ? "green" : "crimson", marginLeft:12 }}>{msg.text}</span>}
                </div>
            </div>
        );

}