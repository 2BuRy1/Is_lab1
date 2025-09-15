import { useEffect, useState } from "react";
import "../styles/table.css";
import { api } from "./api";

const parseTickets = (data) => {
    if (Array.isArray(data)) return data;
    if (Array.isArray(data?.tickets)) return data.tickets;
    return [];
};

const num = (v) => {
    if (v == null) return null;
    if (typeof v === "object" && "parsedValue" in v) return Number(v.parsedValue);
    const n = Number(v);
    return Number.isNaN(n) ? null : n;
};

const val = (t, path, def = "—") => {
    const parts = path.split(".");
    let x = t;
    for (const p of parts) x = x?.[p];
    if (path.endsWith(".x") || path.endsWith(".y") || path.endsWith(".z") || ["price","number","discount","person.weight","venue.capacity","event.ticketsCount","id","person.id","venue.id","event.id","coordinates.x","coordinates.y"].includes(path)) {
        const n = num(x);
        return n == null ? def : n;
    }
    return x ?? def;
};

const columns = [
    { key: "id",            title: "ID",           width: 70,  sortable: true, sortAccessor: (t) => num(t.id) },
    { key: "name",          title: "Название",     width: 180, sortable: true, sortAccessor: (t) => t.name ?? "" },
    { key: "price",         title: "Цена",         width: 90,  sortable: true, sortAccessor: (t) => num(t.price),
        render: (t) => (typeof t.price === "number" ? t.price.toFixed(2) : (num(t.price) ?? "—")) },
    { key: "type",          title: "Тип",          width: 110, sortable: true, sortAccessor: (t) => t.type ?? "" },
    { key: "number",        title: "Кол-во",       width: 80,  sortable: true, sortAccessor: (t) => num(t.number) },
    { key: "discount",      title: "Скидка",       width: 90,  sortable: true, sortAccessor: (t) => num(t.discount),
        render: (t) => (t.discount == null ? "—" : String(t.discount)) },

    { key: "coord_x",       title: "Coord X",      width: 90,  sortable: true, sortAccessor: (t) => num(t.coordinates?.x),
        render: (t) => (num(t.coordinates?.x) ?? "—") },
    { key: "coord_y",       title: "Coord Y",      width: 90,  sortable: true, sortAccessor: (t) => num(t.coordinates?.y),
        render: (t) => (num(t.coordinates?.y) ?? "—") },

    { key: "person_id",         title: "Person ID",         width: 100, sortable: true, sortAccessor: (t) => num(t.person?.id),
        render: (t) => (num(t.person?.id) ?? "—") },
    { key: "person_passport",   title: "PassportID",        width: 140, sortable: true, sortAccessor: (t) => t.person?.passportID ?? "" },
    { key: "person_weight",     title: "Weight",            width: 100, sortable: true, sortAccessor: (t) => num(t.person?.weight),
        render: (t) => (num(t.person?.weight) ?? "—") },
    { key: "person_nat",        title: "Nationality",       width: 140, sortable: true, sortAccessor: (t) => t.person?.nationality ?? "" },
    { key: "person_hair",       title: "HairColor",         width: 120, sortable: true, sortAccessor: (t) => t.person?.hairColor ?? "" },
    { key: "person_eye",        title: "EyeColor",          width: 120, sortable: true, sortAccessor: (t) => t.person?.eyeColor ?? "" },
    { key: "person_loc_x",      title: "Loc X",             width: 90,  sortable: true, sortAccessor: (t) => num(t.person?.location?.x),
        render: (t) => (num(t.person?.location?.x) ?? "—") },
    { key: "person_loc_y",      title: "Loc Y",             width: 90,  sortable: true, sortAccessor: (t) => num(t.person?.location?.y),
        render: (t) => (num(t.person?.location?.y) ?? "—") },
    { key: "person_loc_z",      title: "Loc Z",             width: 90,  sortable: true, sortAccessor: (t) => num(t.person?.location?.z),
        render: (t) => (num(t.person?.location?.z) ?? "—") },

    { key: "event_id",          title: "Event ID",          width: 100, sortable: true, sortAccessor: (t) => num(t.event?.id),
        render: (t) => (num(t.event?.id) ?? "—") },
    { key: "event_name",        title: "Event name",        width: 160, sortable: true, sortAccessor: (t) => t.event?.name ?? "" },
    { key: "event_count",       title: "Tickets count",     width: 130, sortable: true, sortAccessor: (t) => num(t.event?.ticketsCount),
        render: (t) => (num(t.event?.ticketsCount) ?? "—") },
    { key: "event_type",        title: "Event type",        width: 130, sortable: true, sortAccessor: (t) => t.event?.eventType ?? "" },

    { key: "venue_id",          title: "Venue ID",          width: 100, sortable: true, sortAccessor: (t) => num(t.venue?.id),
        render: (t) => (num(t.venue?.id) ?? "—") },
    { key: "venue_name",        title: "Venue name",        width: 160, sortable: true, sortAccessor: (t) => t.venue?.name ?? "" },
    { key: "venue_capacity",    title: "Capacity",          width: 110, sortable: true, sortAccessor: (t) => num(t.venue?.capacity),
        render: (t) => (num(t.venue?.capacity) ?? "—") },
    { key: "venue_type",        title: "Venue type",        width: 120, sortable: true, sortAccessor: (t) => t.venue?.type ?? "" },

    { key: "comment",           title: "Комментарий",       width: 220, sortable: true, sortAccessor: (t) => t.comment ?? "" },
];

function baseCompare(a, b) {
    const nil = (x) => x == null;
    if (nil(a) && nil(b)) return 0;
    if (nil(a)) return -1;
    if (nil(b)) return 1;

    if (Array.isArray(a) || Array.isArray(b)) {
        const A = Array.isArray(a) ? a : [a];
        const B = Array.isArray(b) ? b : [b];
        const len = Math.max(A.length, B.length);
        for (let i = 0; i < len; i++) {
            const r = baseCompare(A[i], B[i]);
            if (r !== 0) return r;
        }
        return 0;
    }

    const toNum = (x) =>
        typeof x === "number" ? x :
            (typeof x === "string" && x.trim() !== "" && !isNaN(x)) ? Number(x) : null;

    const na = toNum(a), nb = toNum(b);
    if (na != null && nb != null) return na - nb;

    return String(a).localeCompare(String(b), undefined, { numeric: true, sensitivity: "base" });
}
const cmpDir = (a, b, dir) => (dir === "desc" ? -baseCompare(a, b) : baseCompare(a, b));

export default function Table({ tableName }) {
    const [data, setData] = useState([]);
    const [status, setStatus] = useState(null);
    const [page, setPage] = useState(1);
    const [size, setSize] = useState(10);
    const [query, setQuery] = useState("");

    const [sortKey, setSortKey] = useState(null);
    const [sortDir, setSortDir] = useState("asc");

    useEffect(() => {
        (async () => {
            try {
                const res = await api.get("/get_tickets");
                setData(parseTickets(res.data));
                setStatus(null);
            } catch (e) {
                setStatus(`Не удалось загрузить: ${e.response?.status || ""} ${e.message}`);
                setData([]);
            }
        })();
    }, []);

    const filtered = (() => {
        const q = query.trim().toLowerCase();
        if (!q) return data;
        return data.filter((t) =>
            columns.some((c) => {
                const value =
                    c.sortAccessor ? c.sortAccessor(t)
                        : t[c.key] ?? "";
                const v =
                    typeof value === "number" ? String(value)
                        : Array.isArray(value) ? JSON.stringify(value)
                            : String(value ?? "");
                return v.toLowerCase().includes(q);
            })
        );
    })();

    const sorted = (() => {
        if (!sortKey) return filtered;
        const col = columns.find((c) => c.key === sortKey);
        const getVal = (t) => (col?.sortAccessor ? col.sortAccessor(t) : t?.[sortKey]);
        const arr = [...filtered];
        arr.sort((a, b) => cmpDir(getVal(a), getVal(b), sortDir));
        return arr;
    })();

    const totalPages = Math.max(1, Math.ceil(sorted.length / size));
    const pageSafe = Math.min(page, totalPages);
    const start = (pageSafe - 1) * size;
    const slice = sorted.slice(start, start + size);
    const goto = (p) => setPage(Math.max(1, Math.min(totalPages, p)));

    const onSort = (col) => {
        if (!col.sortable) return;
        if (sortKey === col.key) setSortDir((d) => (d === "asc" ? "desc" : "asc"));
        else { setSortKey(col.key); setSortDir("asc"); }
        setPage(1);
    };

    return (
        <div className={`${tableName || ""} table-wrap`}>
            <div className="toolbar">
                <div className="toolbar__title">Tickets</div>

                <input
                    className="toolbar__search"
                    placeholder="Поиск по всем колонкам…"
                    value={query}
                    onChange={(e) => { setQuery(e.target.value); setPage(1); }}
                />

                <label className="toolbar__label">На странице:</label>
                <select
                    className="toolbar__select"
                    value={size}
                    onChange={(e) => { setSize(Number(e.target.value)); setPage(1); }}
                >
                    {[5, 10, 20, 50].map((n) => <option key={n} value={n}>{n}</option>)}
                </select>

                <div className="toolbar__pager">
                    <button className="pager-btn" onClick={() => goto(pageSafe - 1)} disabled={pageSafe <= 1}>Prev</button>
                    <span>{pageSafe} / {totalPages}</span>
                    <button className="pager-btn" onClick={() => goto(pageSafe + 1)} disabled={pageSafe >= totalPages}>Next</button>
                </div>
            </div>

            {status && <div className="status">{status}</div>}

            <div className="table-container">
                <div className="table-scroll">
                    <table className="tickets-table">
                        <thead>
                        <tr>
                            {columns.map((c) => (
                                <th
                                    key={c.key}
                                    style={{ "--w": `${c.width}px` }}
                                    className={c.sortable ? "sortable" : ""}
                                    onClick={() => onSort(c)}
                                    title={c.sortable ? "Сортировать" : ""}
                                >
                                    {c.title}
                                    {sortKey === c.key && <span>{sortDir === "asc" ? " ▲" : " ▼"}</span>}
                                </th>
                            ))}
                        </tr>
                        </thead>
                        <tbody>
                        {slice.map((t) => (
                            <tr key={t.id}>
                                {columns.map((c) => {
                                    let content;
                                    switch (c.key) {
                                        case "price": content = columns.find(x=>x.key==="price").render(t); break;
                                        case "discount": content = columns.find(x=>x.key==="discount").render(t); break;
                                        case "coord_x": content = val(t, "coordinates.x"); break;
                                        case "coord_y": content = val(t, "coordinates.y"); break;
                                        case "person_id": content = val(t, "person.id"); break;
                                        case "person_passport": content = val(t, "person.passportID"); break;
                                        case "person_weight": content = val(t, "person.weight"); break;
                                        case "person_nat": content = val(t, "person.nationality"); break;
                                        case "person_hair": content = val(t, "person.hairColor"); break;
                                        case "person_eye": content = val(t, "person.eyeColor"); break;
                                        case "person_loc_x": content = val(t, "person.location.x"); break;
                                        case "person_loc_y": content = val(t, "person.location.y"); break;
                                        case "person_loc_z": content = val(t, "person.location.z"); break;
                                        case "event_id": content = val(t, "event.id"); break;
                                        case "event_name": content = val(t, "event.name"); break;
                                        case "event_count": content = val(t, "event.ticketsCount"); break;
                                        case "event_type": content = val(t, "event.eventType"); break;
                                        case "venue_id": content = val(t, "venue.id"); break;
                                        case "venue_name": content = val(t, "venue.name"); break;
                                        case "venue_capacity": content = val(t, "venue.capacity"); break;
                                        case "venue_type": content = val(t, "venue.type"); break;
                                        default:
                                            content = t[c.key] ?? "—";
                                    }
                                    return (
                                        <td key={c.key} style={{ "--w": `${c.width}px` }}>
                                            {content}
                                        </td>
                                    );
                                })}
                            </tr>
                        ))}
                        {slice.length === 0 && (
                            <tr>
                                <td className="tickets-table__empty" colSpan={columns.length}>
                                    {data.length === 0 ? "Нет данных" : "Ничего не найдено"}
                                </td>
                            </tr>
                        )}
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    );
}