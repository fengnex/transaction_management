import axios from 'axios';
import { Transaction, TransactionFormData } from '../types/transaction';

const API_BASE_URL = 'http://localhost:8080/api/v1/transactions';

export const transactionService = {
  getAllTransactions: async (): Promise<Transaction[]> => {
    const response = await axios.get(API_BASE_URL);
    return response.data.content;
  },

  createTransaction: async (transaction: TransactionFormData): Promise<Transaction> => {
    const response = await axios.post(API_BASE_URL, transaction);
    return response.data;
  },

  updateTransaction: async (id: number, transaction: TransactionFormData): Promise<Transaction> => {
    const response = await axios.put(`${API_BASE_URL}/${id}`, transaction);
    return response.data;
  },

  deleteTransaction: async (id: number): Promise<void> => {
    await axios.delete(`${API_BASE_URL}/${id}`);
  }
}; 