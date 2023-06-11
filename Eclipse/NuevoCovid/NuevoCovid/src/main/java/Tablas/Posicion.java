package Tablas;

import java.util.Objects;

public class Posicion {
	private Integer idPosicion;
	private Double latitud;
	private Double longitud;
	private Integer idUsuario;
	private Integer idZona2;
	
	public Posicion(Integer idPosicion, Double latitud, Double longitud, Integer idUsuario, Integer idZona2) {
		super();
		this.idPosicion = idPosicion;
		this.latitud = latitud;
		this.longitud = longitud;
		this.idUsuario = idUsuario;
		this.idZona2 = idZona2;
	}

	public Integer getIdPosicion() {
		return idPosicion;
	}

	public void setIdPosicion(Integer idPosicion) {
		this.idPosicion = idPosicion;
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

	public Integer getIdUsuario() {
		return idUsuario;
	}

	public void setIdUsuario(Integer idUsuario) {
		this.idUsuario = idUsuario;
	}

	public Integer getidZona2() {
		return idZona2;
	}

	public void setidZona2(Integer idZona2) {
		this.idZona2 = idZona2;
	}

	@Override
	public int hashCode() {
		return Objects.hash(idPosicion, idUsuario, idZona2, latitud, longitud);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Posicion other = (Posicion) obj;
		return Objects.equals(idPosicion, other.idPosicion) && Objects.equals(idUsuario, other.idUsuario)
				&& Objects.equals(idZona2, other.idZona2) && Objects.equals(latitud, other.latitud)
				&& Objects.equals(longitud, other.longitud);
	}

	@Override
	public String toString() {
		return "Posicion [idPosicion=" + idPosicion + ", latitud=" + latitud + ", longitud=" + longitud + ", idUsuario="
				+ idUsuario + ", idZona2=" + idZona2 + "]";
	}

	
	
}

