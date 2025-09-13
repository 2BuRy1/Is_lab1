import { useState } from "react";
import axios from "axios";

const api = axios.create({
    headers: { "Content-Type": "application/json" },
});

const COLORS = ["GREEN", "RED", "ORANGE", "WHITE", "BROWN"];
const COUNTRIES = ["GERMANY", "INDIA", "THAILAND", "SOUTH_KOREA", "JAPAN"];

export default function CreatePersonPanel({ onCreated }) {
    const [v, setV] = useState({
        passportID: "", weight: "", nationality: "",
        hairColor: "", eyeColor: "",
        locX: "", locY: "", locZ: ""
    });
    const [busy, setBusy] = useState(false);
    const [msg, setMsg] = useState(null);

    const set = k => e => setV(s => ({ ...s, [k]: e.target.value }));

    const validate = () => {
        if (!v.passportID.trim()) return "passportID обязателен";
        if (!(Number(v.weight) > 0)) return "weight должен быть > 0";
        if (!v.nationality) return "nationality обязателен";
        if (!v.hairColor) return "hairColor обязателен";
        if (v.locX === "" || v.locZ === "") return "Location: x и z обязательны";
        return null;
    };

    const buildPayload = () => ({
        passportID: v.passportID.trim(),
        weight: Number(v.weight),
        nationality: v.nationality,
        hairColor: v.hairColor,
        eyeColor: v.eyeColor || undefined,
        location: {
            x: Number(v.locX),
            y: v.locY === "" ? 0 : Number(v.locY),
            z: Number(v.locZ)
        }
    });

    const save = async () => {
        setMsg(null);
        const err = validate();
        if (err) { setMsg({ ok:false, text: err }); return; }
        setBusy(true);
        try {
            const res = await api.post("/add_person", buildPayload());
            setMsg({ ok:true, text: `Person id=${res.data?.id ?? "—"} создан` });
            setV({ passportID:"", weight:"", nationality:"", hairColor:"", eyeColor:"", locX:"", locY:"", locZ:"" });
            onCreated?.();
        } catch (e) {
            setMsg({ ok:false, text: `Ошибка: ${e.response?.status || ""} ${e.response?.data || e.message}` });
        } finally { setBusy(false); }
    };

    return (
        <div style={{ display:"grid", gridTemplateColumns:"1fr 2fr", gap:12, maxWidth:760 }}>
            <label>passportID *</label>
            <input value={v.passportID} onChange={set("passportID")} />

            <label>weight *</label>
            <input type="number" step="0.01" min="0.01" value={v.weight} onChange={set("weight")} />

            <label>nationality *</label>
            <select value={v.nationality} onChange={set("nationality")}>
                <option value="">— выберите —</option>
                {COUNTRIES.map(c => <option key={c} value={c}>{c}</option>)}
            </select>

            <label>hairColor *</label>
            <select value={v.hairColor} onChange={set("hairColor")}>
                <option value="">— выберите —</option>
                {COLORS.map(c => <option key={c} value={c}>{c}</option>)}
            </select>

            <label>eyeColor</label>
            <select value={v.eyeColor} onChange={set("eyeColor")}>
                <option value="">— не задан —</option>
                {COLORS.map(c => <option key={c} value={c}>{c}</option>)}
            </select>

            <div style={{ gridColumn: "1 / -1", fontWeight: 600, marginTop: 8 }}>Location</div>

            <label>X *</label>
            <input type="number" step="1" value={v.locX} onChange={set("locX")} />

            <label>Y</label>
            <input type="number" step="0.01" value={v.locY} onChange={set("locY")} />

            <label>Z *</label>
            <input type="number" step="0.01" value={v.locZ} onChange={set("locZ")} />

            <div style={{ gridColumn:"1 / -1", display:"flex", gap:12, alignItems:"center" }}>
                <button onClick={save} disabled={busy}>{busy ? "Сохраняю..." : "Создать Person"}</button>
                {msg && <span style={{ color: msg.ok ? "green" : "crimson" }}>{msg.text}</span>}
            </div>
        </div>
    );
}