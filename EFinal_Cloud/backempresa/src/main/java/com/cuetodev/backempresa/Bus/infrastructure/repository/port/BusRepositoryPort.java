package com.cuetodev.backempresa.Bus.infrastructure.repository.port;


import com.cuetodev.backempresa.Bus.domain.Bus;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

public interface BusRepositoryPort {
    public List<Bus> getAvailableBuses(HashMap<String, Object> conditions);
    public void createUpdateBus(Bus bus);
    public Bus getBusByData(String city, Date date, Float hour);
    public List<Bus> getBookingsByBus(HashMap<String, Object> conditions);
}
