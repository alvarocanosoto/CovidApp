package Tablas;



import java.util.Objects;

public class Usuario {
	private Integer idUsuario;
	private String nombre;
	private String apellidos;
	private String email;
	private String dni;
	private Long fechaNacimiento;
	private String password;
	
	public Usuario(Integer idUsuario, String nombre, String apellidos, String email, String dni, Long fechaNacimiento, String password) {
		super();
		this.idUsuario = idUsuario;
		this.nombre = nombre;
		this.apellidos = apellidos;
		this.email = email;
		this.dni = dni;
		this.fechaNacimiento = fechaNacimiento;
		this.password = password;
	}

	public Integer getIdUsuario() {
		return idUsuario;
	}

	public void setIdUsuario(Integer idUsuario) {
		this.idUsuario = idUsuario;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getApellidos() {
		return apellidos;
	}

	public void setApellidos(String apellidos) {
		this.apellidos = apellidos;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getDni() {
		return dni;
	}

	public void setDni(String dni) {
		this.dni = dni;
	}

	public Long getFechaNacimiento() {
		return fechaNacimiento;
	}

	public void setFechaNacimiento(Long fechaNacimiento) {
		this.fechaNacimiento = fechaNacimiento;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public int hashCode() {
		return Objects.hash(apellidos, dni, email, fechaNacimiento, idUsuario, nombre, password);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Usuario other = (Usuario) obj;
		return Objects.equals(apellidos, other.apellidos) && Objects.equals(dni, other.dni)
				&& Objects.equals(email, other.email) && Objects.equals(fechaNacimiento, other.fechaNacimiento)
				&& Objects.equals(idUsuario, other.idUsuario) && Objects.equals(nombre, other.nombre)
				&& Objects.equals(password, other.password);
	}

	@Override
	public String toString() {
		return "Usuario [idUsuario=" + idUsuario + ", nombre=" + nombre + ", apellidos=" + apellidos + ", email="
				+ email + ", dni=" + dni + ", fechaNacimiento=" + fechaNacimiento + ", password=" + password + "]";
	}

	
	
}