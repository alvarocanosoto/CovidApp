package Tablas;

import java.util.Objects;

public class Zona {
	private Integer idZona;
	private Double latitud;
	private Double longitud;

	
	public Zona(Integer idZona, Double latitud, Double longitud) {
		super();
		this.idZona = idZona;
		this.latitud = latitud;
		this.longitud = longitud;

	}

	public Integer getIdZona() {
		return idZona;
	}

	public void setIdZona(Integer idZona) {
		this.idZona = idZona;
	}

	public Double getLatitud() {
		return latitud;
	}

	public void setLatitud(Double latitud) {
		this.latitud = latitud;
	}

	public Double getLongitud() {
		return longitud;
	}

	public void setLongitud(Double longitud) {
		this.longitud = longitud;
	}

	@Override
	public int hashCode() {
		return Objects.hash(idZona, latitud, longitud);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Zona other = (Zona) obj;
		return Objects.equals(idZona, other.idZona) && Objects.equals(latitud, other.latitud)
				&& Objects.equals(longitud, other.longitud);
	}

	@Override
	public String toString() {
		return "Zona [idZona=" + idZona + ", latitud=" + latitud + ", longitud=" + longitud + "]";
	}



	
}

