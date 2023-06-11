package Tablas;

import java.util.Objects;

public class Placa {
	private Integer idPlaca;
	private String modelo;
	private Integer idZona;
	
	public Placa(Integer idPlaca, String modelo, Integer idZona) {
		super();
		this.idPlaca = idPlaca;
		this.modelo = modelo;
		this.idZona = idZona;
	}

	@Override
	public String toString() {
		return "Placa [idPlaca=" + idPlaca + ", modelo=" + modelo + ", idZona=" + idZona + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(idPlaca, idZona, modelo);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Placa other = (Placa) obj;
		return Objects.equals(idPlaca, other.idPlaca) && Objects.equals(idZona, other.idZona)
				&& Objects.equals(modelo, other.modelo);
	}

	public Integer getIdPlaca() {
		return idPlaca;
	}

	public void setIdPlaca(Integer idPlaca) {
		this.idPlaca = idPlaca;
	}

	public String getModelo() {
		return modelo;
	}

	public void setModelo(String modelo) {
		this.modelo = modelo;
	}

	public Integer getIdZona() {
		return idZona;
	}

	public void setIdZona(Integer idZona) {
		this.idZona = idZona;
	}

	
}

