package ch.fhnw.wodss.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import ch.fhnw.wodss.domain.Board;
import ch.fhnw.wodss.repository.BoardRepository;

@Component
@Transactional
public class BoardService {
	
	@Autowired
	private BoardRepository boardRepository;
	
	public BoardService(){
		super();
	}

	public Board saveBoard(Board board){
		return boardRepository.save(board);
	}
	
	public void deleteBoard(Board board){
		boardRepository.delete(board);
	}
	
	public void deleteBoard(Integer id){
		boardRepository.delete(id);
	}
	
	public List<Board> getAll(){
		return boardRepository.findAll();
	}
	
	public Board getById(Integer id){
		return boardRepository.findOne(id);
	}


}