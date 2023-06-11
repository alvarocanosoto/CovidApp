package Tablas;

import java.util.Objects;

public class Concentracion {
	private Integer idConcentracion;
	private Double concentracion;
	private Long fecha;
	private Integer idPlaca;
	
	public Concentracion(Integer idConcentracion, Double concentracion, Long fecha, Integer idPlaca) {
		super();
		this.idConcentracion = idConcentracion;
		this.concentracion = concentracion;
		this.fecha = fecha;
		this.idPlaca = idPlaca;
	}
	
	public Concentracion() {
		
	}

	public Integer getIdConcentracion() {
		return idConcentracion;
	}

	public void setIdConcentracion(Integer idConcentracion) {
		this.idConcentracion = idConcentracion;
	}

	public Double getConcentracion() {
		return concentracion;
	}

	public void setConcentracion(Double concentracion) {
		this.concentracion = concentracion;
	}

	public Long getFecha() {
		return fecha;
	}

	public void setFecha(Long fecha) {
		this.fecha = fecha;
	}

	public Integer getIdPlaca() {
		return idPlaca;
	}

	public void setIdPlaca(Integer idPlaca) {
		this.idPlaca = idPlaca;
	}

	@Override
	public int hashCode() {
		return Objects.hash(concentracion, fecha, idConcentracion, idPlaca);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Concentracion other = (Concentracion) obj;
		return Objects.equals(concentracion, other.concentracion) && Objects.equals(fecha, other.fecha)
				&& Objects.equals(idConcentracion, other.idConcentracion) && Objects.equals(idPlaca, other.idPlaca);
	}

	@Override
	public String toString() {
		return "Concentracion [idConcentracion=" + idConcentracion + ", concentracion=" + concentracion + ", fecha="
				+ fecha + ", idPlaca=" + idPlaca + "]";
	}
	
	
}

