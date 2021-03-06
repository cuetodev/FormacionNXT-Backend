package com.cuetodev.ej3_1.Persona.infrastructure.repository.port;

import com.cuetodev.ej3_1.Persona.domain.Persona;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.HashMap;
import java.util.List;

public interface PersonaRepositoryPort {
    public Persona save(Persona persona);
    public Persona findPersonaById(int id) throws Exception;
    public List<Persona> findPersonaByUsuario(String usuario) throws Exception;
    public List<Persona> getAllPeople() throws Exception;
    public int deletePerson(int id) throws Exception;
    public Persona updatePerson(Persona personaActualizada) throws Exception;
    public List<Persona> getData(HashMap<String, Object> conditions, String orderBy);
}
