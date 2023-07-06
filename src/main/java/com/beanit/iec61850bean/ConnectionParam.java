package com.beanit.iec61850bean;

import java.util.Objects;

public class ConnectionParam {
    private String iedName;

    private String IP;
    private String IP_SUBNET;
    private String OSI_AP_Title;
    private String OSI_AE_Qualifier;
    private String OSI_PSEL;
    private String OSI_SSEL;
    private String OSI_TSEL;
    private String IP_GATEWAY;
    private String S_Profile;
    private String MAC_Address;


    public String getIedName() {
        return iedName;
    }

    public void setIedName(String iedName) {
        this.iedName = iedName;
    }

    public String getIP() {
        return IP;
    }

    public void setIP(String IP) {
        this.IP = IP;
    }

    public String getIP_SUBNET() {
        return IP_SUBNET;
    }

    public void setIP_SUBNET(String IP_SUBNET) {
        this.IP_SUBNET = IP_SUBNET;
    }

    public String getOSI_AP_Title() {
        return OSI_AP_Title;
    }

    public void setOSI_AP_Title(String OSI_AP_Title) {
        this.OSI_AP_Title = OSI_AP_Title;
    }

    public String getOSI_AE_Qualifier() {
        return OSI_AE_Qualifier;
    }

    public void setOSI_AE_Qualifier(String OSI_AE_Qualifier) {
        this.OSI_AE_Qualifier = OSI_AE_Qualifier;
    }

    public String getOSI_PSEL() {
        return OSI_PSEL;
    }

    public void setOSI_PSEL(String OSI_PSEL) {
        this.OSI_PSEL = OSI_PSEL;
    }

    public String getOSI_SSEL() {
        return OSI_SSEL;
    }

    public void setOSI_SSEL(String OSI_SSEL) {
        this.OSI_SSEL = OSI_SSEL;
    }

    public String getOSI_TSEL() {
        return OSI_TSEL;
    }

    public void setOSI_TSEL(String OSI_TSEL) {
        this.OSI_TSEL = OSI_TSEL;
    }

    public String getIP_GATEWAY() {
        return IP_GATEWAY;
    }

    public void setIP_GATEWAY(String IP_GATEWAY) {
        this.IP_GATEWAY = IP_GATEWAY;
    }

    public String getS_Profile() {
        return S_Profile;
    }

    public void setS_Profile(String s_Profile) {
        S_Profile = s_Profile;
    }

    public String getMAC_Address() {
        return MAC_Address;
    }

    public void setMAC_Address(String MAC_Address) {
        this.MAC_Address = MAC_Address;
    }


    @Override
    public String toString() {
        return "iedName = " + iedName + '\n' +
                "IP = " + IP + '\n' +
                "IP_SUBNET = " + IP_SUBNET + '\n' +
                "OSI_AP_Title = " + OSI_AP_Title + '\n' +
                "OSI_AE_Qualifier = " + OSI_AE_Qualifier + '\n' +
                "OSI_PSEL = " + OSI_PSEL + '\n' +
                "OSI_SSEL = " + OSI_SSEL + '\n' +
                "OSI_TSEL = " + OSI_TSEL + '\n' +
                "IP_GATEWAY = " + IP_GATEWAY + '\n' +
                "S_Profile = " + S_Profile + '\n' +
                "MAC-Address = " + MAC_Address
                ;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConnectionParam that = (ConnectionParam) o;
        return iedName.equals(that.iedName) &&
                IP.equals(that.IP) &&
                Objects.equals(IP_SUBNET, that.IP_SUBNET) &&
                Objects.equals(OSI_AP_Title, that.OSI_AP_Title) &&
                Objects.equals(OSI_AE_Qualifier, that.OSI_AE_Qualifier) &&
                Objects.equals(OSI_PSEL, that.OSI_PSEL) &&
                Objects.equals(OSI_SSEL, that.OSI_SSEL) &&
                Objects.equals(OSI_TSEL, that.OSI_TSEL) &&
                Objects.equals(IP_GATEWAY, that.IP_GATEWAY) &&
                Objects.equals(S_Profile, that.S_Profile) &&
                Objects.equals(MAC_Address, that.MAC_Address);
    }

    @Override
    public int hashCode() {
        return Objects.hash(iedName, IP, IP_SUBNET, OSI_AP_Title, OSI_AE_Qualifier, OSI_PSEL, OSI_SSEL,
                OSI_TSEL, IP_GATEWAY, S_Profile, MAC_Address);
    }
}