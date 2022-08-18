package com.ssafy.api.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ssafy.db.entity.Sido;
import com.ssafy.db.repository.SidoRepository;

@Service("sidoService")
public class SidoServiceImpl implements SidoService{
	@Autowired
	SidoRepository sidoRepository;
	
	@Override
	public List<Sido> findAll() {
		return sidoRepository.findAll();
	}
	
	@Override
	public Optional<Sido> findById(Long id) {
		return sidoRepository.findById(id);
	}
	
}
