package Tablas;

import java.util.Objects;

public class Ayuda {
	private Integer idZona;
	private Integer idPlaca;
	private Double concentracion;
	
	
	
	public Ayuda(Integer idZona, Integer idPlaca, Double concentracion) {
		super();
		this.idZona = idZona;
		this.idPlaca = idPlaca;
		this.concentracion = concentracion;
	}
	
	public Ayuda() {
		
	}

	public Integer getIdZona() {
		return idZona;
	}

	public void setIdZona(Integer idZona) {
		this.idZona = idZona;
	}

	public Integer getIdPlaca() {
		return idPlaca;
	}

	public void setIdPlaca(Integer idPlaca) {
		this.idPlaca = idPlaca;
	}
	
	public Double getConcentracion() {
		return concentracion;
	}

	public void setConcentracion(Double concentracion) {
		this.concentracion = concentracion;
	}


	@Override
	public int hashCode() {
		return Objects.hash(idZona, idPlaca, concentracion);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Ayuda other = (Ayuda) obj;
		return Objects.equals(idZona, other.idZona) && Objects.equals(idPlaca, other.idPlaca)
				&& Objects.equals(concentracion, other.concentracion);
	}

	@Override
	public String toString() {
		return "Ayuda [idZona=" + idZona + ", idPlaca=" + idPlaca + ", concentracion="
				+ concentracion + "]";
	}
	
	
}