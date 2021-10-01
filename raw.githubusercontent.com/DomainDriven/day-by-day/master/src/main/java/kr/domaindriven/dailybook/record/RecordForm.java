package kr.domaindriven.dailybook.record;

import java.time.LocalDateTime;

/**
 * <p>
 * </p>
 *
 * @author Younghoe Ahn
 */
public class RecordForm {

    private LocalDateTime date;
    private Integer amount;
    private String summary;
    private RecordCategory category;

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public void setCategory(RecordCategory category) {
        this.category = category;
    }

    public LocalDateTime getDate() {

        return date;
    }

    public Integer getAmount() {
        return amount;
    }

    public String getSummary() {
        return summary;
    }

    public RecordCategory getCategory() {
        return category;
    }
}
