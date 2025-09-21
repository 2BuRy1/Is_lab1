package systems.project.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import systems.project.controllers.api.TicketsApi;
import systems.project.models.Ticket;
import systems.project.models.api.AbstractResponse;
import systems.project.models.api.CloneRequest;
import systems.project.models.api.SellRequestDTO;
import systems.project.models.envelopes.TicketsEnvelope;
import systems.project.services.TicketEventService;
import systems.project.services.TicketService;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@CrossOrigin(origins = {"*"})
public class TicketsApiController implements TicketsApi {

    private final TicketService ticketService;
    private final TicketEventService events;

    public TicketsApiController(TicketService ticketService, TicketEventService events) {
        this.ticketService = ticketService;
        this.events = events;
    }

    @Override
    public ResponseEntity<AbstractResponse> addTicket(Ticket ticket) {
        try {
            Map<String, Boolean> res = ticketService.addTicket(ticket).join();
            boolean ok = Boolean.TRUE.equals(res.get("status"));
            if (ok) {
                events.publishChange("add", null);
                return ResponseEntity.ok(
                        AbstractResponse.builder()
                                .status("ok")
                                .title("Успех")
                                .message("Билет создан")
                                .build()
                );
            }
            return ResponseEntity.badRequest().body(
                    AbstractResponse.builder()
                            .status("error")
                            .title("Ошибка")
                            .message("Ошибка при создании билета")
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    AbstractResponse.builder()
                            .status("error")
                            .title("Ошибка")
                            .message(e.getMessage())
                            .build()
            );
        }
    }

    @Override
    public ResponseEntity<AbstractResponse<Ticket>> cloneVip(CloneRequest cloneRequest) {
        try {
            Ticket copy = ticketService.cloneVip(cloneRequest.getTicketId()).join();
            if (copy != null) {
                events.publishChange("vip-clone", null);
                return ResponseEntity.ok(
                        AbstractResponse.<Ticket>builder()
                                .status("ok")
                                .title("Успех")
                                .message("VIP-копия создана")
                                .data(copy)
                                .build()
                );
            }
            return ResponseEntity.badRequest().body(
                    AbstractResponse.<Ticket>builder()
                            .status("error")
                            .title("Не найдено")
                            .message("Исходный билет не найден")
                            .data(null)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    AbstractResponse.<Ticket>builder()
                            .status("error")
                            .title("Ошибка")
                            .message(e.getMessage())
                            .data(null)
                            .build()
            );
        }
    }

    @Override
    public ResponseEntity<AbstractResponse<Integer>> countCommentLess(String comment) {
        try {
            Map<String, Long> map = ticketService.countByCommentLess(comment).join();
            long cnt = Optional.ofNullable(map.get("count")).orElse(0L);
            return ResponseEntity.ok(
                    AbstractResponse.<Integer>builder()
                            .status("ok")
                            .title("Успех")
                            .message("Подсчитано")
                            .data((int) cnt)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    AbstractResponse.<Integer>builder()
                            .status("error")
                            .title("Ошибка")
                            .message(e.getMessage())
                            .data(null)
                            .build()
            );
        }
    }

    @Override
    public ResponseEntity<AbstractResponse> deleteByComment(String commentEq) {
        try {
            boolean ok = ticketService.deleteAllByComment(commentEq).join();
            if (ok) {
                events.publishChange("bulk-delete", null);
                return ResponseEntity.ok(
                        AbstractResponse.builder()
                                .status("ok")
                                .title("Успех")
                                .message("Удаление по комментарию выполнено")
                                .build()
                );
            }
            return ResponseEntity.badRequest().body(
                    AbstractResponse.builder()
                            .status("error")
                            .title("Не найдено")
                            .message("Не найдено билетов с таким comment")
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    AbstractResponse.builder()
                            .status("error")
                            .title("Ошибка")
                            .message(e.getMessage())
                            .build()
            );
        }
    }

    @Override
    public ResponseEntity<AbstractResponse> deleteTicket(Integer id) {
        try {
            boolean ok = ticketService.removeTicket(id).join();
            if (ok) {
                events.publishChange("delete", id);
                return ResponseEntity.ok(
                        AbstractResponse.builder()
                                .status("ok")
                                .title("Успех")
                                .message("Билет удалён")
                                .build()
                );
            }
            return ResponseEntity.badRequest().body(
                    AbstractResponse.builder()
                            .status("error")
                            .title("Не найдено")
                            .message("Ошибка при удалении объекта, возможно его не существует")
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    AbstractResponse.builder()
                            .status("error")
                            .title("Ошибка")
                            .message(e.getMessage())
                            .build()
            );
        }
    }

    @Override
    public ResponseEntity<AbstractResponse<Ticket>> getTicketById(Integer id) {
        try {
            Ticket t = ticketService.getTicket(id).join();
            if (t != null) {
                return ResponseEntity.ok(
                        AbstractResponse.<Ticket>builder()
                                .status("ok")
                                .title("Успех")
                                .message("Билет найден")
                                .data(t)
                                .build()
                );
            }
            return ResponseEntity.badRequest().body(
                    AbstractResponse.<Ticket>builder()
                            .status("error")
                            .title("Не найдено")
                            .message("Билет не найден")
                            .data(null)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    AbstractResponse.<Ticket>builder()
                            .status("error")
                            .title("Ошибка")
                            .message(e.getMessage())
                            .data(null)
                            .build()
            );
        }
    }

    @Override
    public ResponseEntity<AbstractResponse<TicketsEnvelope>> getTickets() {
        try {
            Map<String, List<Ticket>> map = ticketService.getTickets().join();
            List<Ticket> tickets = map.get("tickets");
            TicketsEnvelope env = new TicketsEnvelope();
            env.setTicketList(tickets);

            return ResponseEntity.ok(
                    AbstractResponse.<TicketsEnvelope>builder()
                            .status("ok")
                            .title("Успех")
                            .message("Список билетов")
                            .data(env)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    AbstractResponse.<TicketsEnvelope>builder()
                            .status("error")
                            .title("Ошибка")
                            .message(e.getMessage())
                            .data(null)
                            .build()
            );
        }
    }

    @Override
    public ResponseEntity<AbstractResponse<Ticket>> minEventTicket() {
        try {
            Ticket t = ticketService.getWithMinEvent().join();
            if (t != null) {
                return ResponseEntity.ok(
                        AbstractResponse.<Ticket>builder()
                                .status("ok")
                                .title("Успех")
                                .message("Минимальный по событию билет")
                                .data(t)
                                .build()
                );
            }
            return ResponseEntity.badRequest().body(
                    AbstractResponse.<Ticket>builder()
                            .status("error")
                            .title("Не найдено")
                            .message("Не найден билет с событием")
                            .data(null)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    AbstractResponse.<Ticket>builder()
                            .status("error")
                            .title("Ошибка")
                            .message(e.getMessage())
                            .data(null)
                            .build()
            );
        }
    }

    @Override
    public ResponseEntity<AbstractResponse> sellTicket(SellRequestDTO req) {
        try {
            boolean ok = ticketService.sellTicket(req.getTicketId(), req.getPersonId(), req.getAmount()).join();
            if (ok) {
                events.publishChange("ticket-sell", null);
                return ResponseEntity.ok(
                        AbstractResponse.builder()
                                .status("ok")
                                .title("Успех")
                                .message("Билет продан")
                                .build()
                );
            }
            return ResponseEntity.badRequest().body(
                    AbstractResponse.builder()
                            .status("error")
                            .title("Ошибка")
                            .message("Продажа не выполнена")
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    AbstractResponse.builder()
                            .status("error")
                            .title("Ошибка")
                            .message(e.getMessage())
                            .build()
            );
        }
    }

    @Override
    public SseEmitter stream() {
        return events.subscribe();
    }


    @Override
    public ResponseEntity<AbstractResponse> updateTicket(Integer id, Ticket ticket) {
        try {
            boolean ok = ticketService.updateTicket(id, ticket).join();
            if (ok) {
                events.publishChange("update", id);
                return ResponseEntity.ok(
                        AbstractResponse.builder()
                                .status("ok")
                                .title("Успех")
                                .message("Билет обновлён")
                                .build()
                );
            }
            return ResponseEntity.badRequest().body(
                    AbstractResponse.builder()
                            .status("error")
                            .title("Не найдено")
                            .message("Билет не найден или не обновлён")
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    AbstractResponse.builder()
                            .status("error")
                            .title("Ошибка")
                            .message(e.getMessage())
                            .build()
            );
        }
    }
}