import { useEffect, useState } from "react";
import axios from "axios";
import "../styles/table.css";

const api = axios.create({
    headers: { "Content-Type": "application/json" }
});

const parseTickets = (data) => {
    if (Array.isArray(data)) return data;
    if (Array.isArray(data?.tickets)) return data.tickets;
    return [];
};


const columns = [
    { key: "id", title: "ID", width: 70, sortable: true, sortAccessor: (t) => Number(t.id) },
    { key: "name", title: "Название", width: 200, sortable: true, sortAccessor: (t) => t.name ?? "" },
    { key: "price", title: "Цена", width: 90, sortable: true, sortAccessor: (t) => Number(t.price),
        render: (t) => (typeof t.price === "number" ? t.price.toFixed(2) : t.price) },
    { key: "type", title: "Тип", width: 110, sortable: true, sortAccessor: (t) => t.type ?? "" },
    { key: "number", title: "Кол-во", width: 80, sortable: true, sortAccessor: (t) => Number(t.number) },
    { key: "discount", title: "Скидка", width: 90, sortable: true, sortAccessor: (t) => t.discount == null ? null : Number(t.discount),
        render: (t) => t.discount != null ? `${t.discount}` : "—" },
    { key: "coords", title: "Координаты", width: 140, sortable: true,
        sortAccessor: (t) => t.coordinates ? [Number(t.coordinates.x), Number(t.coordinates.y)] : [Number.NEGATIVE_INFINITY, Number.NEGATIVE_INFINITY],
        render: (t) => t.coordinates ? `x:${t.coordinates.x} y:${t.coordinates.y}` : "—" },
    { key: "person", title: "Person", width: 610, sortable: true,
        sortAccessor: (t) => {
            const p = t.person;
            return p ? [String(p.passportID ?? ""), Number(p.id ?? Number.POSITIVE_INFINITY)] : ["", Number.POSITIVE_INFINITY];
        },
        render: (t) => t.person
            ? `id:${t.person.id} weight:${t.person.weight} nationality:${t.person.nationality} passport:${t.person.passportID} location:(x:${t.person.location?.x} y:${t.person.location?.y} z:${t.person.location?.z})`
            : "—" },
    { key: "event", title: "Event", width: 360, sortable: true,
        sortAccessor: (t) => {
            const e = t.event;
            return e ? [String(e.name ?? ""), Number(e.id ?? Number.POSITIVE_INFINITY)] : ["", Number.POSITIVE_INFINITY];
        },
        render: (t) => t.event
            ? `id:${t.event.id} name:${t.event.name} ticketsCount:${t.event.ticketsCount} eventType:${t.event.eventType ?? "—"}`
            : "—" },
    { key: "venue", title: "Venue", width: 360, sortable: true,
        // по названию площадки, потом по id
        sortAccessor: (t) => {
            const v = t.venue;
            return v ? [String(v.name ?? ""), Number(v.id ?? Number.POSITIVE_INFINITY)] : ["", Number.POSITIVE_INFINITY];
        },
        render: (t) => t.venue
            ? `id:${t.venue.id} name:${t.venue.name} capacity:${t.venue.capacity} venueType:${t.venue.type ?? "—"}`
            : "—" },
    { key: "comment", title: "Комментарий", width: 240, sortable: true, sortAccessor: (t) => t.comment ?? "" }
];

function baseCompare(a, b) {
    const isNullish = (x) => x == null;
    if (isNullish(a) && isNullish(b)) return 0;
    if (isNullish(a)) return -1;
    if (isNullish(b)) return 1;

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

    const numOrNull = (x) => (typeof x === "number" ? x : (typeof x === "string" && x.trim() !== "" && !isNaN(x) ? Number(x) : null));
    const na = numOrNull(a), nb = numOrNull(b);
    if (na != null && nb != null) {
        if (na < nb) return -1;
        if (na > nb) return 1;
        return 0;
    }

    if (typeof a === "boolean" && typeof b === "boolean") {
        if (a === b) return 0;
        return a ? 1 : -1;
    }

    const sa = String(a);
    const sb = String(b);
    return sa.localeCompare(sb, undefined, { numeric: true, sensitivity: "base" });
}

function compareWithDir(a, b, dir) {
    const r = baseCompare(a, b);
    return dir === "desc" ? -r : r;
}

const Table = ({ tableName }) => {
    const [data, setData] = useState([]);
    const [status, setStatus] = useState(null);
    const [page, setPage] = useState(1);
    const [size, setSize] = useState(10);
    const [query, setQuery] = useState("");

    const [sortKey, setSortKey] = useState(null);   // например, "price"
    const [sortDir, setSortDir] = useState("asc");  // "asc" | "desc"

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
                let cell;
                try {
                    cell = c.render ? c.render(t) : t[c.key];
                } catch {
                    cell = "";
                }
                if (cell == null) return false;
                if (typeof cell === "number") cell = String(cell);
                else if (typeof cell === "object") cell = JSON.stringify(cell);
                return String(cell).toLowerCase().includes(q);
            })
        );
    })();

    const sorted = (() => {
        if (!sortKey) return filtered;
        const col = columns.find(c => c.key === sortKey);
        const getVal = (t) => col?.sortAccessor ? col.sortAccessor(t) : (t ? t[sortKey] : undefined);
        const arr = [...filtered];
        arr.sort((a, b) => compareWithDir(getVal(a), getVal(b), sortDir));
        return arr;
    })();

    const totalPages = Math.max(1, Math.ceil(sorted.length / size));
    const pageSafe = Math.min(page, totalPages);
    const start = (pageSafe - 1) * size;
    const slice = sorted.slice(start, start + size);

    const goto = (p) => setPage(Math.max(1, Math.min(totalPages, p)));


    const onSort = (col) => {
        if (!col.sortable) return;
        if (sortKey === col.key) {
            setSortDir((d) => (d === "asc" ? "desc" : "asc"));
        } else {
            setSortKey(col.key);
            setSortDir("asc");
        }
        setPage(1);
    };

    return (
        <div className={`${tableName} table-wrap`}>
            <div className="toolbar">
                <div className="toolbar__title">Tickets</div>

                <input
                    className="toolbar__search"
                    placeholder="Поиск по всем колонкам…"
                    value={query}
                    onChange={(e) => { setQuery(e.target.value); setPage(1); }}
                />

                <label>На странице:&nbsp;</label>
                <select
                    value={size}
                    onChange={(e) => { setSize(Number(e.target.value)); setPage(1); }}
                >
                    {[5, 10, 20, 50].map(n => <option key={n} value={n}>{n}</option>)}
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
                            {columns.map(c => (
                                <th
                                    key={c.key}
                                    style={{ "--w": `${c.width}px`, cursor: c.sortable ? "pointer" : "default" }}
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
                                {columns.map(c => (
                                    <td key={c.key} style={{ "--w": `${c.width}px` }}>
                                        {c.render ? c.render(t) : (t[c.key] ?? "—")}
                                    </td>
                                ))}
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
};

export default Table;