import { useState } from "react";
import Table from "../components/Table";
import CreateTicketPanel from "../components/CreateTicketForm";
import CreateEventPanel from "../components/CreateEventPanel";
import CreateVenuePanel from "../components/CreateVenuePanel";
import CreatePersonPanel from "../components/CreatePersonPanel";

const MainPage = () => {
    const [reloadKey, setReloadKey] = useState(0);

    const bump = () => setReloadKey(k => k + 1);

    return (
        <div style={{ padding: 16, display: "grid", gap: 24 }}>
            <section>
                <h2>Создать билет</h2>
                <CreateTicketPanel onCreated={bump} />
            </section>

            <section>
                <h3>Создать событие (Event)</h3>
                <CreateEventPanel onCreated={bump} />
            </section>

            <section>
                <h3>Создать площадку (Venue)</h3>
                <CreateVenuePanel onCreated={bump} />
            </section>

            <section>
                <h3>Создать человека (Person)</h3>
                <CreatePersonPanel onCreated={bump} />
            </section>

            <hr />

            <Table key={reloadKey} tableName="mainTable" />
        </div>
    );
};

export default MainPage;