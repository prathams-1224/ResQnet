from datetime import datetime
from sqlalchemy import Column, Integer, String, Float, Boolean, DateTime, Text, JSON
from backend.app.database.connection import Base

class PredictionRecord(Base):
    __tablename__ = "prediction_records"

    id = Column(Integer, primary_key=True, index=True)
    location_id = Column(String(50), index=True, nullable=False)
    timestamp = Column(DateTime, default=datetime.utcnow, index=True)
    weather_observed_at = Column(String(50), nullable=True)
    
    # Risk predictions
    risk_probability = Column(Float, nullable=False)  # Calibrated probability
    baseline_probability = Column(Float, nullable=True)
    risk_level = Column(String(20), nullable=False)    # LOW, MODERATE_ALERT, HIGH_WARNING
    risk_category_code = Column(Integer, default=0)    # 0, 1, 2
    
    # Input factors and explanations
    forecast_precip_24h_mm = Column(Float, nullable=True)
    past_72h_precip_mm = Column(Float, nullable=True)
    soil_saturation_index = Column(Float, nullable=True)
    top_factors = Column(JSON, nullable=True)
    input_features = Column(JSON, nullable=True)
    
    # Provenance
    model_version = Column(String(20), default="1.0.0")
    data_source = Column(String(100), default="Open-Meteo Forecast & Sensor Alignment")
    data_freshness_status = Column(String(20), default="LIVE")  # LIVE, CACHED, STALE, FALLBACK
    is_experimental = Column(Boolean, default=True)


class AlertEvent(Base):
    __tablename__ = "alert_events"

    id = Column(Integer, primary_key=True, index=True)
    location_id = Column(String(50), index=True, nullable=False)
    location_name = Column(String(100), nullable=False)
    timestamp = Column(DateTime, default=datetime.utcnow, index=True)
    
    severity = Column(String(20), nullable=False)  # INFO, ADVISORY, WARNING, EMERGENCY
    title = Column(String(150), nullable=False)
    message = Column(Text, nullable=False)
    
    rule_triggered = Column(String(100), nullable=False)
    trigger_value = Column(Float, nullable=True)
    threshold_value = Column(Float, nullable=True)
    
    source = Column(String(50), default="ResQNet Warning Engine")
    is_official_override = Column(Boolean, default=False)
    is_test = Column(Boolean, default=False)
    
    # Suppression & deduplication
    suppressed_count = Column(Integer, default=0)
    acknowledged = Column(Boolean, default=False)


class OfficialAdvisory(Base):
    __tablename__ = "official_advisories"

    id = Column(Integer, primary_key=True, index=True)
    district = Column(String(50), index=True, default="Dakshina Kannada")
    alert_color = Column(String(20), default="GREEN")  # GREEN, YELLOW, ORANGE, RED
    issuing_agency = Column(String(100), default="India Meteorological Department (IMD) / KSNDMC")
    issued_at = Column(DateTime, default=datetime.utcnow)
    valid_until = Column(DateTime, nullable=True)
    description = Column(Text, nullable=False)
    is_active = Column(Boolean, default=True)


class ShelterOccupancy(Base):
    __tablename__ = "shelter_occupancies"

    id = Column(Integer, primary_key=True, index=True)
    shelter_id = Column(String(50), unique=True, index=True, nullable=False)
    district_id = Column(String(50), index=True, nullable=True)
    name = Column(String(150), nullable=True)
    is_operational = Column(Boolean, default=False, index=True)
    total_capacity = Column(Integer, nullable=True)  # Baseline registry capacity (unverified)
    verified_operational_capacity = Column(Integer, nullable=True)  # Actual staffed capacity confirmed by authorities
    occupied_count = Column(Integer, default=0, nullable=False)
    reserved_count = Column(Integer, default=0, nullable=False)
    is_simulated = Column(Boolean, default=False)
    verification_notes = Column(Text, nullable=True)
    updated_by = Column(String(100), default="SEOC Baseline Registry")
    updated_at = Column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)

    def calculate_status(self):
        if not self.is_operational or self.verified_operational_capacity is None:
            return "UNVERIFIED", 0, None

        cap = self.verified_operational_capacity
        available = max(0, cap - self.occupied_count - self.reserved_count)
        occupancy_ratio = (self.occupied_count + self.reserved_count) / max(1, cap)
        pct = min(100.0, round(occupancy_ratio * 100.0, 1))

        if available == 0 or pct >= 100.0:
            status = "FULL"
        elif pct >= 75.0 or available <= max(5, int(cap * 0.15)):
            status = "LIMITED"
        else:
            status = "AVAILABLE"

        return status, available, pct


class ShelterReservation(Base):
    __tablename__ = "shelter_reservations"

    id = Column(String(60), primary_key=True, index=True)  # e.g. "RES-xxxxxxxx"
    shelter_id = Column(String(50), index=True, nullable=False)
    group_size = Column(Integer, default=1, nullable=False)
    status = Column(String(20), default="ACTIVE", index=True)  # ACTIVE, CHECKED_IN, CANCELLED, EXPIRED
    created_at = Column(DateTime, default=datetime.utcnow, index=True)
    expires_at = Column(DateTime, nullable=False, index=True)
    checked_in_at = Column(DateTime, nullable=True)
    cancelled_at = Column(DateTime, nullable=True)
    citizen_contact = Column(String(100), nullable=True)
    notes = Column(Text, nullable=True)
