package com.something.service.serviceimpl;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.something.domain.HistoryWallet;
import com.something.repo.HistoryWalletRepo;
import com.something.service.HistoryWalletService;

@Service
public class HistoryWalletServiceImpl implements HistoryWalletService {
	@Autowired
	HistoryWalletRepo hwRepo;

	@Override
	public HistoryWallet findById(int id) {
		// TODO Auto-generated method stub
		return hwRepo.findByWalletId(id);
	}

	@Override
	public HistoryWallet update(HistoryWallet hw) {
		// TODO Auto-generated method stub
		return hwRepo.save(hw);
	}

	@Override
	public List<HistoryWallet> findCommissionHistoryByUsername(String username) {
		// TODO Auto-generated method stub
		List<HistoryWallet> listHistoryCommission = hwRepo.findByUsername(username);
		Collections.reverse(listHistoryCommission);

		List<HistoryWallet> result = listHistoryCommission.stream()
				.filter(item -> !"Buy Package".equals(item.getType()) && !"Transfer".equals(item.getType()) && !"Withdraw".equals(item.getType()) && !"Deposit".equals(item.getType()) && !"Swap commission".equals(item.getType())).collect(Collectors.toList());
		
		return result;
	}

	@Override
	public List<HistoryWallet> findSwapHistoryByUsername(String username) {
		// TODO Auto-generated method stub
		List<HistoryWallet> listHistoryCommission = hwRepo.findByUsername(username);
		Collections.reverse(listHistoryCommission);

		List<HistoryWallet> result = listHistoryCommission.stream()
				.filter(item -> "Swap commission".equals(item.getType())).collect(Collectors.toList());

		return result;
	}

	@Override
	public List<HistoryWallet> findDepositHistoryByUsername(String username) {
		// TODO Auto-generated method stub
		List<HistoryWallet> listHistoryCommission = hwRepo.findByUsername(username);
		Collections.reverse(listHistoryCommission);

		List<HistoryWallet> result = listHistoryCommission.stream()
				.filter(item -> "Deposit".equals(item.getType())).collect(Collectors.toList());

		return result;
	}

	@Override
	public List<HistoryWallet> findWithdrawHistoryByUsername(String username) {
		// TODO Auto-generated method stub
		List<HistoryWallet> listHistoryCommission = hwRepo.findByUsername(username);
		Collections.reverse(listHistoryCommission);

		List<HistoryWallet> result = listHistoryCommission.stream()
				.filter(item -> "Withdraw".equals(item.getType())).collect(Collectors.toList());

		return result;
	}

	@Override
	public List<HistoryWallet> findTransferHistoryByUsername(String username) {
		// TODO Auto-generated method stub
		List<HistoryWallet> listHistoryCommission = hwRepo.findByUsername(username);
		Collections.reverse(listHistoryCommission);

		List<HistoryWallet> result = listHistoryCommission.stream()
				.filter(item -> "Transfer".equals(item.getType())).collect(Collectors.toList());

		return result;
	}

}
