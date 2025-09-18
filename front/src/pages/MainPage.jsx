import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Table from '../components/Table';
import '../styles/main.scss';
import { api } from '../components/api';

export default function MainPage() {
  const [reloadKey, setReloadKey] = useState(0);
  const [busy, setBusy] = useState(false);
  const [msg, setMsg] = useState(null);

  const [searchId, setSearchId] = useState('');
  const [found, setFound] = useState(null);

  const [editId, setEditId] = useState('');
  const [editErr, setEditErr] = useState('');

  const [deleteId, setDeleteId] = useState('');
  const [deleteErr, setDeleteErr] = useState('');

  const navigate = useNavigate();
  const bump = () => setReloadKey((k) => k + 1);

  const asId = (v) => {
    if (typeof v !== 'string') return NaN;
    const n = Number(v.trim());
    return Number.isInteger(n) && n > 0 ? n : NaN;
  };

  const onEditPageClick = () => {
    const id = asId(editId);
    if (Number.isNaN(id)) {
      setEditErr('Введите положительный целый ID');
      return;
    }
    setEditErr('');
    navigate(`/tickets/${id}/edit`);
  };

  const onDeleteClick = async () => {
    const id = asId(deleteId);
    if (Number.isNaN(id)) {
      setDeleteErr('Введите положительный целый ID');
      return;
    }
    setDeleteErr('');
    if (!window.confirm(`Удалить билет ${id}?`)) return;

    setBusy(true);
    setMsg(null);
    try {
      await api.delete(`/delete_ticket/${id}`);
      setMsg({ ok: true, text: `Билет ${id} удалён` });
      setDeleteId('');
      bump();
    } catch (e) {
      setMsg({
        ok: false,
        text: `${e.response.data ? e.response.data.title + ' ' + e.response.data.errorMessage : ' '}`,
      });
    } finally {
      setBusy(false);
    }
  };

  const onSearchById = async () => {
    const id = asId(searchId);
    if (Number.isNaN(id)) {
      setMsg({ ok: false, text: 'Введите корректный ID' });
      return;
    }

    setBusy(true);
    setMsg(null);
    try {
      const res = await api.get(`/get_ticket/${id}`);
      setFound(res.data);
    } catch {
      setFound(null);
      setMsg({ ok: false, text: 'Ошибка. Объект не найден' });
    } finally {
      setBusy(false);
    }
  };

  const clearSearch = () => {
    setSearchId('');
    setFound(null);
  };

  return (
    <div className="page">
      <div className="toolbar">
        <button className="btn" onClick={() => navigate('/tickets/new')} disabled={busy}>
          Создать билет
        </button>

        <div className="inline-form">
          <input
            className={`input-id ${editErr ? 'err' : ''}`}
            placeholder="ID для редактирования"
            inputMode="numeric"
            value={editId}
            onChange={(e) => {
              setEditId(e.target.value);
              setEditErr('');
            }}
          />
          <button className="btn" onClick={onEditPageClick} disabled={busy}>
            Редактировать
          </button>
        </div>
        {editErr && <span className="help-err">{editErr}</span>}

        <div className="inline-form">
          <input
            className={`input-id ${deleteErr ? 'err' : ''}`}
            placeholder="ID для удаления"
            inputMode="numeric"
            value={deleteId}
            onChange={(e) => {
              setDeleteId(e.target.value);
              setDeleteErr('');
            }}
          />
          <button className="btn btn-danger" onClick={onDeleteClick} disabled={busy}>
            Удалить
          </button>
        </div>
        {deleteErr && <span className="help-err">{deleteErr}</span>}

        <button className="btn" onClick={() => navigate('/functions')} disabled={busy}>
          Дополнительные функции
        </button>

        <div className="inline-form">
          <input
            className="search-input"
            placeholder="ID билета"
            inputMode="numeric"
            value={searchId}
            onChange={(e) => setSearchId(e.target.value)}
          />
          <button className="btn" onClick={onSearchById} disabled={busy}>
            Найти
          </button>
          <button className="btn btn-secondary" onClick={clearSearch}>
            Очистить
          </button>
        </div>

        {msg && <span className={msg.ok ? 'status-ok' : 'status-err'}>{msg.text}</span>}
      </div>

      {found && (
        <div className="details-card">
          <h3>Билет #{found.id}</h3>
          <p>
            <b>Название:</b> {found.name}
          </p>
          <p>
            <b>Цена:</b> {found.price}
          </p>
          <p>
            <b>Тип:</b> {found.type}
          </p>
          <p>
            <b>Количество:</b> {found.number}
          </p>
          <p>
            <b>Скидка:</b> {found.discount ?? '—'}
          </p>
          <p>
            <b>Комментарий:</b> {found.comment ?? '—'}
          </p>
          <p>
            <b>Координаты:</b> X={found.coordinates?.x}, Y={found.coordinates?.y}
          </p>

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
