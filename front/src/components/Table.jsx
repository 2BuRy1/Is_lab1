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
    { key: "id",      title: "ID",         width: 70 },
    { key: "name",    title: "Название",   width: 200 },
    { key: "price",   title: "Цена",       width: 90,
        render: (t) => (typeof t.price === "number" ? t.price.toFixed(2) : t.price) },
    { key: "type",    title: "Тип",        width: 110 },
    { key: "number",  title: "Кол-во",     width: 80 },
    { key: "discount",title: "Скидка",     width: 90,
        render: (t) => t.discount != null ? `${t.discount}` : "—" },
    { key: "coords",  title: "Координаты", width: 140,
        render: (t) => t.coordinates ? `x:${t.coordinates.x} y:${t.coordinates.y}` : "—" },
    { key: "person",  title: "Person",     width: 110,
        render: (t) => t.person?.id ?? "—" },
    { key: "event",   title: "Event",      width: 160,
        render: (t) => t.event?.name ?? t.event?.id ?? "—" },
    { key: "venue",   title: "Venue",      width: 160,
        render: (t) => t.venue?.name ?? t.venue?.id ?? "—" },
    { key: "comment", title: "Комментарий", width: 240,
        render: (t) => t.comment || "—" },
];

const Table = ({ tableName }) => {
    const [data, setData] = useState([]);
    const [status, setStatus] = useState(null);
    const [page, setPage] = useState(1);
    const [size, setSize] = useState(10);
    const [query, setQuery] = useState("");

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
        return data.filter(t =>
            (t.name || "").toLowerCase().includes(q) ||
            (t.comment || "").toLowerCase().includes(q)
        );
    })();

    const totalPages = Math.max(1, Math.ceil(filtered.length / size));
    const pageSafe = Math.min(page, totalPages);

    const start = (pageSafe - 1) * size;
    const slice = filtered.slice(start, start + size);

    const goto = (p) => setPage(Math.max(1, Math.min(totalPages, p)));

    return (
        <div className={`${tableName} table-wrap`}>

            <div className="toolbar">
                <div className="toolbar__title">Tickets</div>

                <input
                    className="toolbar__search"
                    placeholder="Поиск по названию/комментарию…"
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
                                <th key={c.key} style={{ "--w": `${c.width}px` }}>{c.title}</th>
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