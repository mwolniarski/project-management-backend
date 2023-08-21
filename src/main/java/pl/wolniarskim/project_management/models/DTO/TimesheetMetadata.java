package pl.wolniarskim.project_management.models.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TimesheetMetadata {
    private long numberOfAllBillableHours;
    private long numberOfAllNonBillableHours;
    private long numberOfBillableHoursInCurrentMonth;
    private long numberOfNonBillableHoursInCurrentMonth;
}
