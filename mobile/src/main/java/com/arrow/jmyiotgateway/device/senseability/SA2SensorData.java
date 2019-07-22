package com.arrow.jmyiotgateway.device.senseability;

import com.arrow.jmyiotgateway.cloud.iot.IotParameter;
import com.arrow.jmyiotgateway.device.TelemetriesNames;
import com.arrow.jmyiotgateway.device.sensortile.data.SensorData;

public class SA2SensorData extends SensorData<SenseAbilityDataModel> {

    public SA2SensorData(SenseAbilityDataModel data) {
        super(data);
    }

    @Override
    public IotParameter[] toIotParameters() {
        return new IotParameter[]{
                new IotParameter(TelemetriesNames.TEMPERATURE, Double.toString(getData().getTemperature())),
                new IotParameter(TelemetriesNames.HUMIDITY, Double.toString(getData().getHumidity())),
                new IotParameter(TelemetriesNames.PRESSURE, Double.toString(getData().getPressure())),
                new IotParameter(TelemetriesNames.STATUS, getData().getStatus()),
                new IotParameter(TelemetriesNames.MAGNET, Boolean.toString(getData().isMagnet())),
                new IotParameter(TelemetriesNames.AIRFLOW, Double.toString(getData().getAirflow()))
        };
    }
}
