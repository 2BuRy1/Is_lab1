package systems.project.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;
import systems.project.controllers.api.VenuesApi;
import systems.project.models.Venue;
import systems.project.models.api.AbstractResponse;
import systems.project.models.envelopes.VenuesEnvelope;
import systems.project.services.VenueService;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = {"*"})
public class VenuesApiController implements VenuesApi {

    private final VenueService venueService;

    public VenuesApiController(VenueService venueService) {
        this.venueService = venueService;
    }

    @Override
    public ResponseEntity<AbstractResponse> addVenue(Venue venue) {
        try {
            Map<String, Boolean> res = venueService.addVenue(venue).join();
            boolean ok = Boolean.TRUE.equals(res.get("status"));
            if (ok) {
                return ResponseEntity.ok(
                        AbstractResponse.builder()
                                .status("ok")
                                .title("Успех")
                                .message("Площадка создана")
                                .build()
                );
            }
            return ResponseEntity.badRequest().body(
                    AbstractResponse.builder()
                            .status("error")
                            .title("Ошибка")
                            .message("Ошибка при создании площадки")
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
    public ResponseEntity<AbstractResponse<VenuesEnvelope>> getVenues() {
        try {
            Map<String, List<Venue>> map = venueService.getVenues().join();
            List<Venue> venues = map.get("venues");
            VenuesEnvelope envelope = new VenuesEnvelope();
            envelope.setVenueList(venues);

            return ResponseEntity.ok(
                    AbstractResponse.<VenuesEnvelope>builder()
                            .status("ok")
                            .title("Успех")
                            .message("Список площадок")
                            .data(envelope)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    AbstractResponse.<VenuesEnvelope>builder()
                            .status("error")
                            .title("Ошибка")
                            .message(e.getMessage())
                            .data(null)
                            .build()
            );
        }
    }
}