package fpt.aptech.management_field.services;

import fpt.aptech.management_field.models.SystemSettings;
import fpt.aptech.management_field.payload.dtos.SystemSettingsDto;
import fpt.aptech.management_field.repositories.SystemSettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class SettingsService {

    @Autowired
    private SystemSettingsRepository settingsRepository;

    public SystemSettingsDto getSystemSettings() {
        SystemSettingsDto dto = new SystemSettingsDto();
        
        dto.setMaintenanceMode(getBooleanSetting("maintenance_mode", false));
        dto.setDefaultCurrency(getStringSetting("default_currency", "USD"));
        dto.setMaxBookingHours(getIntegerSetting("max_booking_hours", 24));
        dto.setSiteName(getStringSetting("site_name", "PlayerConnect"));
        dto.setSupportEmail(getStringSetting("support_email", "support@playerconnect.com"));
        dto.setEmailNotifications(getBooleanSetting("email_notifications", true));
        dto.setSmsNotifications(getBooleanSetting("sms_notifications", false));
        dto.setPlatformCommission(getDoubleSetting("platform_commission", 5.0));
        dto.setMaxCancellationHours(getIntegerSetting("max_cancellation_hours", 2));
        dto.setAutoApproveBookings(getBooleanSetting("auto_approve_bookings", false));
        
        return dto;
    }

    @Transactional
    public SystemSettingsDto updateSystemSettings(SystemSettingsDto dto) {
        if (dto.getMaintenanceMode() != null) {
            saveSetting("maintenance_mode", dto.getMaintenanceMode().toString(), "BOOLEAN", "Enable/disable maintenance mode");
        }
        if (dto.getDefaultCurrency() != null) {
            saveSetting("default_currency", dto.getDefaultCurrency(), "STRING", "Default currency for the platform");
        }
        if (dto.getMaxBookingHours() != null) {
            saveSetting("max_booking_hours", dto.getMaxBookingHours().toString(), "INTEGER", "Maximum hours for a single booking");
        }
        if (dto.getSiteName() != null) {
            saveSetting("site_name", dto.getSiteName(), "STRING", "Name of the website");
        }
        if (dto.getSupportEmail() != null) {
            saveSetting("support_email", dto.getSupportEmail(), "STRING", "Support email address");
        }
        if (dto.getEmailNotifications() != null) {
            saveSetting("email_notifications", dto.getEmailNotifications().toString(), "BOOLEAN", "Enable/disable email notifications");
        }
        if (dto.getSmsNotifications() != null) {
            saveSetting("sms_notifications", dto.getSmsNotifications().toString(), "BOOLEAN", "Enable/disable SMS notifications");
        }
        if (dto.getPlatformCommission() != null) {
            saveSetting("platform_commission", dto.getPlatformCommission().toString(), "DOUBLE", "Platform commission percentage");
        }
        if (dto.getMaxCancellationHours() != null) {
            saveSetting("max_cancellation_hours", dto.getMaxCancellationHours().toString(), "INTEGER", "Maximum hours before booking to allow cancellation");
        }
        if (dto.getAutoApproveBookings() != null) {
            saveSetting("auto_approve_bookings", dto.getAutoApproveBookings().toString(), "BOOLEAN", "Automatically approve new bookings");
        }
        
        return getSystemSettings();
    }

    private String getStringSetting(String key, String defaultValue) {
        Optional<SystemSettings> setting = settingsRepository.findByKey(key);
        return setting.map(SystemSettings::getValue).orElse(defaultValue);
    }

    private Boolean getBooleanSetting(String key, Boolean defaultValue) {
        Optional<SystemSettings> setting = settingsRepository.findByKey(key);
        if (setting.isPresent()) {
            return Boolean.parseBoolean(setting.get().getValue());
        }
        return defaultValue;
    }

    private Integer getIntegerSetting(String key, Integer defaultValue) {
        Optional<SystemSettings> setting = settingsRepository.findByKey(key);
        if (setting.isPresent()) {
            try {
                return Integer.parseInt(setting.get().getValue());
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    private Double getDoubleSetting(String key, Double defaultValue) {
        Optional<SystemSettings> setting = settingsRepository.findByKey(key);
        if (setting.isPresent()) {
            try {
                return Double.parseDouble(setting.get().getValue());
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    private void saveSetting(String key, String value, String type, String description) {
        Optional<SystemSettings> existingSetting = settingsRepository.findByKey(key);
        
        if (existingSetting.isPresent()) {
            SystemSettings setting = existingSetting.get();
            setting.setValue(value);
            settingsRepository.save(setting);
        } else {
            SystemSettings newSetting = new SystemSettings();
            newSetting.setKey(key);
            newSetting.setValue(value);
            newSetting.setType(type);
            newSetting.setDescription(description);
            settingsRepository.save(newSetting);
        }
    }
}