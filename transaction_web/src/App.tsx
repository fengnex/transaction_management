import React from 'react';
import { Layout, Button, Modal, message } from 'antd';
import { PlusOutlined } from '@ant-design/icons';
import { useRequest } from 'ahooks';
import TransactionList from './components/TransactionList';
import TransactionForm from './components/TransactionForm';
import { transactionService } from './services/transactionService';
import { Transaction } from './types/transaction';

const { Header, Content } = Layout;

const App: React.FC = () => {
  const [isModalVisible, setIsModalVisible] = React.useState(false);
  const [editingTransaction, setEditingTransaction] = React.useState<Transaction | undefined>();

  const { data: transactions, run: refreshTransactions } = useRequest(
    transactionService.getAllTransactions,
    {
      manual: false,
    }
  );

  const handleAddNew = () => {
    setEditingTransaction(undefined);
    setIsModalVisible(true);
  };

  const handleEdit = (transaction: Transaction) => {
    setEditingTransaction(transaction);
    setIsModalVisible(true);
  };

  const handleDelete = async (id: number) => {
    try {
      await transactionService.deleteTransaction(id);
      message.success('Transaction deleted successfully');
      refreshTransactions();
    } catch (error) {
      message.error('Failed to delete transaction');
    }
  };

  const handleSubmit = async (values: any) => {
    try {
      if (editingTransaction) {
        console.log("values-update=",values);
        await transactionService.updateTransaction(editingTransaction.id, values);
        message.success('Transaction updated successfully');
      } else {    
        console.log("values-add=",values);    
        await transactionService.createTransaction(values);
        message.success('Transaction created successfully');
      }
      setIsModalVisible(false);
      refreshTransactions();
    } catch (error) {
      console.error("err=",error);
      message.error('Failed to save transaction');
    }
  };

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Header style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
        <h1 style={{ color: 'white', margin: 0 }}>Transaction Management</h1>
        <Button type="primary" icon={<PlusOutlined />} onClick={handleAddNew}>
          Add Transaction
        </Button>
      </Header>
      <Content style={{ padding: '24px' }}>
        <TransactionList
          transactions={transactions || []}
          onEdit={handleEdit}
          onDelete={handleDelete}
        />
        <Modal
          title={editingTransaction ? 'Edit Transaction' : 'Add Transaction'}
          open={isModalVisible}
          onCancel={() => setIsModalVisible(false)}
          footer={null}
        >
          <TransactionForm
            initialValues={editingTransaction}
            onSubmit={handleSubmit}
            onCancel={() => setIsModalVisible(false)}
          />
        </Modal>
      </Content>
    </Layout>
  );
};

export default App;
