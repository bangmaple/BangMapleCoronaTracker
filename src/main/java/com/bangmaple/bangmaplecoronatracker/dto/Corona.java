/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bangmaple.bangmaplecoronatracker.dto;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Date;

/**
 *
 * @author bangmaple
 */
public final class Corona implements java.io.Serializable {

    private Latest_data latest_data;
    private String code;
    private String updated_at;
    private Today today;
    private Coordinates coordinates;
    private String name;
    private Long population;

    protected final class Coordinates {

        private Double latitude;
        private Double longitude;

        public Double getLatitude() {
            return latitude;
        }

        public Double getLongitude() {
            return longitude;
        }

    }

    public final class Today {

        private Integer confirmed;
        private Integer deaths;

        public Integer getConfirmed() {
            return confirmed;
        }

        public Integer getDeaths() {
            return deaths;
        }
    }

    public final class Latest_data {

        private Integer recovered;
        private Integer critical;
        private Integer confirmed;
        private Calculated calculated;
        private Integer deaths;

        public final class Calculated {

            private Double recovery_rate;
            private Double cases_per_million_population;
            private Double recovered_vs_death_ratio;
            private Double death_rate;

            public Double getDeath_rate() {
                return death_rate;
            }

            public Double getRecovery_rate() {
                return recovery_rate;
            }

            public Double getCases_per_million_population() {
                return cases_per_million_population;
            }

            public Double getRecovered_vs_death_ratio() {
                return recovered_vs_death_ratio;
            }

        }

        public Integer getRecovered() {
            return recovered;
        }

        public Integer getCritical() {
            return critical;
        }

        public Integer getConfirmed() {
            return confirmed;
        }

        public Calculated getCalculated() {
            return calculated;
        }

        public Integer getDeaths() {
            return deaths;
        }
    }

    public Latest_data getLatest_data() {
        return latest_data;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getUpdated_at() {
        return updated_at;
    }

    public Today getToday() {
        return today;
    }

    public String getName() {
        return name;
    }

    public Long getPopulation() {
        return population;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
         DateTimeFormatter timeFormatter = DateTimeFormatter.ISO_DATE_TIME;
        TemporalAccessor accessor = timeFormatter.parse(updated_at);
        String updated = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(Date.from(Instant.from(accessor)));
        sb.append(code).append(",").append(name).append(",").append(population).append(",")
                .append(latest_data.confirmed).append(",").append(latest_data.recovered).append(",")
                .append(latest_data.deaths).append(",").append(latest_data.critical).append(",")
                .append(String.format("%.4f",latest_data.calculated.recovery_rate)).append(",")
                .append(String.format("%.4f",latest_data.calculated.cases_per_million_population))
                .append(",").append(String.format("%.4f",latest_data.calculated.death_rate)).append(",")
                .append(String.format("%.4f",latest_data.calculated.recovered_vs_death_ratio))
                .append(",").append(updated).append(",").append(String.format("%.4f",coordinates.latitude)).append(",")
                .append(String.format("%.4f",coordinates.longitude));
        return sb.toString();
    }

}
