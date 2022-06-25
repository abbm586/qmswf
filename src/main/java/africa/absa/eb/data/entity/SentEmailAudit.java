package africa.absa.eb.data.entity;

import java.time.LocalDateTime;
import javax.persistence.Entity;

@Entity
public class SentEmailAudit extends AbstractEntity {

    private Integer quote_number;
    private String ab_number;
    private String destination_email;
    private LocalDateTime sent_datetime;

    public Integer getQuote_number() {
        return quote_number;
    }
    public void setQuote_number(Integer quote_number) {
        this.quote_number = quote_number;
    }
    public String getAb_number() {
        return ab_number;
    }
    public void setAb_number(String ab_number) {
        this.ab_number = ab_number;
    }
    public String getDestination_email() {
        return destination_email;
    }
    public void setDestination_email(String destination_email) {
        this.destination_email = destination_email;
    }
    public LocalDateTime getSent_datetime() {
        return sent_datetime;
    }
    public void setSent_datetime(LocalDateTime sent_datetime) {
        this.sent_datetime = sent_datetime;
    }

}
