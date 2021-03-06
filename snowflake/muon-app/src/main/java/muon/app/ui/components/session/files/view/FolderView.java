package muon.app.ui.components.session.files.view;

import javax.swing.*;
import javax.swing.RowSorter.SortKey;
import javax.swing.border.LineBorder;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;

import muon.app.App;
import muon.app.common.FileInfo;
import muon.app.common.FileType;
import muon.app.ui.components.SkinnedScrollPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

public class FolderView extends JPanel {
	public static final Logger log = LoggerFactory.getLogger(FolderView.class);
	// private DefaultListModel<FileInfo> listModel;
//    private ListView list;
	private FolderViewTableModel folderViewModel;
	private JTable table;
	private JScrollPane tableScroller, listScroller;
	private JList<FileInfo> fileList;
	// private TableRowSorter<FolderViewTableModel> sorter;
	private FolderViewEventListener listener;
	private JPopupMenu popup;
	private boolean showHiddenFiles = false;
	// private int sortIndex = 2;
//    private boolean sortAsc = false;
	private List<FileInfo> files;
	private TableRowSorter<? extends Object> sorter;

	public FolderView(FolderViewEventListener listener, Consumer<String> statusCallback) {
		super(new BorderLayout());
		this.listener = listener;
		this.popup = new JPopupMenu();

		showHiddenFiles = App.getGlobalSettings().isShowHiddenFilesByDefault();

//        listModel = new DefaultListModel<>();
//        list = new ListView(listModel);
//        list.setCellRenderer(new FolderViewListCellRenderer());

		folderViewModel = new FolderViewTableModel(false);

		// TableCellTextRenderer r = new TableCellTextRenderer();

		TableCellLabelRenderer r1 = new TableCellLabelRenderer();

		table = new JTable(folderViewModel);
		table.setSelectionForeground(App.SKIN.getDefaultSelectionForeground());
		table.setDefaultRenderer(FileInfo.class, r1);
		table.setDefaultRenderer(Long.class, r1);
		table.setDefaultRenderer(LocalDateTime.class, r1);
		table.setDefaultRenderer(Object.class, r1);
		table.setFillsViewportHeight(true);
		table.setShowGrid(false);

		listener.install(this);

		table.setIntercellSpacing(new Dimension(0, 0));
		table.setDragEnabled(true);
		table.setDropMode(DropMode.ON);
		// table.setShowGrid(false);
		// table.setRowHeight(r.getPreferredHeight());
		table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		sorter = new TableRowSorter<>(table.getModel());

		// compare name
		sorter.setComparator(0, new Comparator<>() {
			@Override
			public int compare(Object o1, Object o2) {
				FileInfo s1 = (FileInfo) o1;
				FileInfo s2 = (FileInfo) o2;
				return s1.compareTo(s2);
			}
		});

		// compare size
		sorter.setComparator(2, new Comparator<>() {
			@Override
			public int compare(Object o1, Object o2) {
				Long s1 = (Long) ((FileInfo) o1).getSize();
				Long s2 = (Long) ((FileInfo) o2).getSize();
				return s1.compareTo(s2);
			}
		});

		// compare type
		sorter.setComparator(3, new Comparator<>() {
			@Override
			public int compare(Object o1, Object o2) {
				String s1 = (String) ((FileInfo) o1).getType().toString();
				String s2 = (String) ((FileInfo) o2).getType().toString();
				return s1.compareTo(s2);
			}
		});

		// compare last modified
		sorter.setComparator(1, new Comparator<>() {
			@Override
			public int compare(Object o1, Object o2) {
				FileInfo s1 = (FileInfo) o1;
				FileInfo s2 = (FileInfo) o2;

				if (s1.getType() == FileType.Directory || s1.getType() == FileType.DirLink) {
					if (s2.getType() == FileType.Directory || s2.getType() == FileType.DirLink) {
						return s1.getLastModified().compareTo(s2.getLastModified());
					} else {
						return 1;
					}
				} else {
					if (s2.getType() == FileType.Directory || s2.getType() == FileType.DirLink) {
						return -1;
					} else {
						return s1.getLastModified().compareTo(s2.getLastModified());
					}
				}
			}
		});

		// compare permission
		sorter.setComparator(4, new Comparator<>() {
			@Override
			public int compare(Object o1, Object o2) {
				String s1 = (String) ((FileInfo) o1).getPermissionString().toString();
				String s2 = (String) ((FileInfo) o2).getPermissionString().toString();
				return s1.compareTo(s2);
			}
		});

		// compare owner
		sorter.setComparator(5, new Comparator<>() {
			@Override
			public int compare(Object o1, Object o2) {
				String s1 = (String) ((FileInfo) o1).getPermissionString().toString();
				String s2 = (String) ((FileInfo) o2).getPermissionString().toString();
				return s1.compareTo(s2);
			}
		});

		table.setRowSorter(sorter);

		this.sort(1, SortOrder.DESCENDING);

		//
		//
		// table.setAutoCreateRowSorter(true);

//		List<RowSorter.SortKey> sortKeys = new ArrayList<>();
//		sortKeys.add(new RowSorter.SortKey(1, SortOrder.DESCENDING));
//		sorter.setSortKeys(sortKeys);
//		sorter.sort();

		// sorter=new TableRowSorter<>(folderViewModel);

//        sorter.setComparator(0,(a,b)->{
//            log.info("called new sorter");
//            return 1;
//        });
//        sorter.setComparator(0, new Comparator<Object>() {
//            @Override
//            public int compare(Object s1, Object s2) {
//                FileInfo info1 = (FileInfo) s1;
//                FileInfo info2 = (FileInfo) s2;
//                if (info1.getType() == FileType.Directory || info1.getType() == FileType.DirLink) {
//                    if (info2.getType() == FileType.Directory || info2.getType() == FileType.DirLink) {
//                        return info1.getName().compareToIgnoreCase(info2.getName());
//                    } else {
//                        return 1;
//                    }
//                } else {
//                    if (info2.getType() == FileType.Directory || info2.getType() == FileType.DirLink) {
//                        return -1;
//                    } else {
//                        return info1.getName().compareToIgnoreCase(info2.getName());
//                    }
//                }
//            }
//        });
//
//        sorter.setComparator(1, new Comparator<Long>() {
//            @Override
//            public int compare(Long s1, Long s2) {
//                log.info("Sorter 1 called");
//                return s1.compareTo(s2);
//            }
//        });
//
//        sorter.setComparator(3, new Comparator<FileInfo>() {
//
//            @Override
//            public int compare(FileInfo info1, FileInfo info2) {
//                log.info("Sorter 3 called");
//                if (info1.getType() == FileType.Directory || info1.getType() == FileType.DirLink) {
//                    if (info2.getType() == FileType.Directory || info2.getType() == FileType.DirLink) {
//                        return info1.getLastModified().compareTo(info2.getLastModified());
//                    } else {
//                        return 1;
//                    }
//                } else {
//                    if (info2.getType() == FileType.Directory || info2.getType() == FileType.DirLink) {
//                        return -1;
//                    } else {
//                        return info1.getLastModified().compareTo(info2.getLastModified());
//                    }
//                }
//            }
//
//        });

////        sorter = new TableRowSorter<FolderViewTableModel>(folderViewModel);
//        sorter.setRowFilter(new RowFilter<FolderViewTableModel, Integer>() {
//            @Override
//            public boolean include(Entry<? extends FolderViewTableModel, ? extends Integer> entry) {
//                return true;
//            }
//        });
//
//        sorter.setComparator(0, (Object o1, Object o2) -> {
//            FileInfo s1 = (FileInfo) o1;
//            FileInfo s2 = (FileInfo) o2;
//            log.info("Name sorter called");
//            return s1.toString().compareTo(s2.toString());
//        });
//
//        sorter.setComparator(0, (FileInfo o1, FileInfo o2) -> {
//            FileInfo s1 = (FileInfo) o1;
//            FileInfo s2 = (FileInfo) o2;
//            log.info("Name sorter called");
//            return s1.toString().compareTo(s2.toString());
//        });
//
//        sorter.setComparator(0, (String o1, String o2) -> {
//            log.info("Name sorter called");
//            return o1.toString().compareTo(o2.toString());
//        });
//
//        sorter.setComparator(1, (Long s1, Long s2) -> {
//            log.info("Size sorter called");
//            return s1.compareTo(s2);
//        });
//
//        sorter.setComparator(2, (Object s1, Object s2) -> {
//            log.info("Type sorter called");
//            return (s1 + "").compareTo((s2 + ""));
//        });
//
//        sorter.setComparator(3,
//                new Comparator<Object>() {
//                    @Override
//                    public int compare(Object o1, Object o2) {
//                        FileInfo s1 = (FileInfo) o1;
//                        FileInfo s2 = (FileInfo) o2;
//                        log.info("Date sorter called");
//                        if (s1.getType() == FileType.Directory || s1.getType() == FileType.DirLink) {
//                            if (s2.getType() == FileType.Directory || s2.getType() == FileType.DirLink) {
//                                return s1.getLastModified().compareTo(s2.getLastModified());
//                            } else {
//                                return 1;
//                            }
//                        } else {
//                            if (s1.getType() == FileType.Directory || s1.getType() == FileType.DirLink) {
//                                return -1;
//                            } else {
//                                return s1.getLastModified().compareTo(s2.getLastModified());
//                            }
//                        }
//                    }
//                });
//
//        table.setRowSorter(sorter);
//
////        sorter.setComparator(3, (FileInfo s1, FileInfo s2) -> {
////            log.info("Date sorter called");
////            if (s1.getType() == FileType.Directory || s1.getType() == FileType.DirLink) {
////                if (s2.getType() == FileType.Directory || s2.getType() == FileType.DirLink) {
////                    return s1.getLastModified().compareTo(s2.getLastModified());
////                } else {
////                    return 1;
////                }
////            } else {
////                if (s1.getType() == FileType.Directory || s1.getType() == FileType.DirLink) {
////                    return -1;
////                } else {
////                    return s1.getLastModified().compareTo(s2.getLastModified());
////                }
////            }
////        });
//
//        sorter.setComparator(4, (Object s1, Object s2) -> {
//            log.info("Perm sorter called");
//            return (s1 + "").compareTo((s2 + ""));
//        });
//
//        sorter.setComparator(5, (Object s1, Object s2) -> {
//            log.info("Extra sorter called");
//            return (s1 + "").compareTo((s2 + ""));
//        });
//
//        table.setRowSorter(sorter);
//
//        ArrayList<RowSorter.SortKey> list = new ArrayList<>();
//        list.add(new RowSorter.SortKey(3, SortOrder.DESCENDING));
//        sorter.setSortKeys(list);
//
//        sorter.sort();
//

		table.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
				.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "Enter");
		table.getActionMap().put("Enter", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				FileInfo[] files = getSelectedFiles();
				if (files.length > 0) {
					if (files[0].getType() == FileType.Directory || files[0].getType() == FileType.DirLink) {
						String str = files[0].getPath();
						listener.render(str, App.getGlobalSettings().isDirectoryCache());
					}
				}
			}
		});

		table.getSelectionModel().addListSelectionListener(e -> {
			if (e.getValueIsAdjusting()) {
				return;
			}
			int rc = table.getSelectedRowCount();
			int tc = table.getRowCount();
			
			String text = String.format("%d of %d selected", rc, tc);
			statusCallback.accept(text);
		});

		table.addKeyListener(new FolderViewKeyHandler(table, folderViewModel));

		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				log.info("Mouse click on table");
				if (table.getSelectionModel().getValueIsAdjusting()) {
					log.info("Value adjusting");
					selectRow(e);
					return;
				}
				if (e.getClickCount() == 2) {
					Point p = e.getPoint();
					int r = table.rowAtPoint(p);
					int x = table.getSelectedRow();
					if (x == -1) {
						return;
					}
					if (r == table.getSelectedRow()) {
						FileInfo fileInfo = folderViewModel.getItemAt(getRow(r));
						if (fileInfo.getType() == FileType.Directory || fileInfo.getType() == FileType.DirLink) {
							listener.addBack(fileInfo.getPath());
							listener.render(fileInfo.getPath(), App.getGlobalSettings().isDirectoryCache());
						} else {
							listener.openApp(fileInfo);
						}
					}
				} else if (e.isPopupTrigger() || e.getButton() == MouseEvent.BUTTON3) {
					selectRow(e);
					log.info("called");
					listener.createMenu(popup, getSelectedFiles());
					popup.pack();
					popup.show(table, e.getX(), e.getY());
				}
			}
		});

//        list.setVisibleRowCount(-1);
//        list.setDragEnabled(true);
//        list.setVisibleRowCount(20);
//
//        list.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
//                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "Enter");
//        list.getActionMap().put("Enter", new AbstractAction() {
//            @Override
//            public void actionPerformed(ActionEvent ae) {
//                FileInfo[] files = getSelectedFiles();
//                if (files.length > 0) {
//                    if (files[0].getType() == FileType.Directory || files[0].getType() == FileType.DirLink) {
//                        String str = files[0].getPath();
//                        log.info("Rendering: " + str);
//                        listener.addBack(str);
//                        listener.render(str);
//                    }
//                }
//            }
//        });
//
//        list.addMouseListener(new MouseAdapter() {
//            @Override
//            public void mousePressed(MouseEvent e) {
//                log.info("Mouse click on list");
//                if (list.getSelectionModel().getValueIsAdjusting()) {
//                    log.info("List Value adjusting");
//                    return;
//                }
//
//                if (e.getClickCount() == 2) {
//                    log.info("Double click");
//                    Point p = e.getPoint();
//                    int r = list.locationToIndex(p);// folderTable.rowAtPoint(p);
//                    int x = list.getSelectedIndex();// folderTable.getSelectedRow();
//                    if (x == -1) {
//                        log.info("List no row selected");
//                        return;
//                    }
//                    if (r == list.getSelectedIndex()) {
//                        FileInfo fileInfo = listModel.getElementAt(r);
//                        if (fileInfo.getType() == FileType.Directory || fileInfo.getType() == FileType.DirLink) {
//                            listener.addBack(fileInfo.getPath());
//                            listener.render(fileInfo.getPath());
//                        } else {
//                            listener.openApp(fileInfo);
//                        }
//                    }
//                } else if (e.isPopupTrigger() || e.getButton() == MouseEvent.BUTTON3) {
//                    log.info("popup called");
//                    listener.createMenu(popup, getSelectedFiles());
//                    popup.pack();
//                    popup.show(list, e.getX(), e.getY());
//                }
//            }
//        });

		resizeColumnWidth(table);

		// table.setBorder(null);
		tableScroller = new SkinnedScrollPane(table);

//        JScrollBar verticalScroller = new JScrollBar(JScrollBar.VERTICAL);
//        verticalScroller.setUI(new CustomScrollBarUI());
//
//        //verticalScroller.putClientProperty("Nimbus.Overrides", App.scrollBarSkin);
//        scrollPane.setVerticalScrollBar(verticalScroller);
//
//        JScrollBar horizontalScroller = new JScrollBar(JScrollBar.HORIZONTAL);
//        horizontalScroller.setUI(new CustomScrollBarUI());
//        scrollPane.setHorizontalScrollBar(horizontalScroller);

//		scrollPane
//				.setBorder(new LineBorder(App.SKIN.getDefaultBorderColor(), 1));

		table.setRowHeight(r1.getHeight());

		resizeColumnWidth(table);

		log.info("Row height: " + r1.getHeight());

		fileList = new JList<>(folderViewModel);
		fileList.setBackground(App.SKIN.getTableBackgroundColor());
		fileList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
		fileList.setVisibleRowCount(-1);
		fileList.setCellRenderer(new FolderViewListCellRenderer());
		listScroller = new SkinnedScrollPane(fileList);

		fileList.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				log.info("Mouse click on table");
				if (fileList.getSelectionModel().getValueIsAdjusting()) {
					log.info("Value adjusting");
					selectListRow(e);
					return;
				}
				if (e.getClickCount() == 2) {
					Point p = e.getPoint();
					int r = fileList.locationToIndex(p);// table.rowAtPoint(p);
					int x = fileList.getSelectedIndex();// table.getSelectedRow();
					if (x == -1) {
						return;
					}
					if (r == x) {
						FileInfo fileInfo = folderViewModel.getItemAt(getRow(r));
						if (fileInfo.getType() == FileType.Directory || fileInfo.getType() == FileType.DirLink) {
							listener.addBack(fileInfo.getPath());
							listener.render(fileInfo.getPath(), App.getGlobalSettings().isDirectoryCache());
						} else {
							listener.openApp(fileInfo);
						}
					}
				} else if (e.isPopupTrigger() || e.getButton() == MouseEvent.BUTTON3) {
					selectRow(e);
					log.info("called");
					listener.createMenu(popup, getSelectedFiles());
					popup.pack();
					popup.show(table, e.getX(), e.getY());
				}
			}
		});

		refreshViewMode();

//		table.getModel().addTableModelListener(e -> {
//			int rc = table.getSelectedRowCount();
//			int tc = table.getRowCount();
//			String text = String.format("Total %d file(s)", tc);
//			statusCallback.accept(text);
//		});
	}

	private void selectRow(MouseEvent e) {
		int r = table.rowAtPoint(e.getPoint());
		log.info("Row at point: " + r);
		if (r == -1) {
			table.clearSelection();
		} else {
			if (table.getSelectedRowCount() > 0) {
				int[] rows = table.getSelectedRows();
				for (int row : rows) {
					if (r == row) {
						return;
					}
				}
			}
			table.setRowSelectionInterval(r, r);
		}
	}

	private void selectListRow(MouseEvent e) {
		int r = fileList.locationToIndex(e.getPoint());// table.rowAtPoint(e.getPoint());
		log.info("Row at point: " + r);
		if (r == -1) {
			fileList.clearSelection();
		} else {
			if (fileList.getSelectedIndices().length > 0) {
				int[] rows = fileList.getSelectedIndices();
				for (int row : rows) {
					if (r == row) {
						return;
					}
				}
			}
			fileList.setSelectedIndex(r);
		}
	}

	public FileInfo[] getSelectedFiles() {
		int indexes[] = table.getSelectedRows();
		FileInfo fs[] = new FileInfo[indexes.length];
		int i = 0;
		for (int index : indexes) {
			FileInfo info = folderViewModel.getItemAt(table.convertRowIndexToModel(index));
			fs[i++] = info;
		}
		return fs;

//        List<FileInfo> lst = list.getSelectedValuesList();
//        FileInfo fs[] = new FileInfo[lst.size()];
//        int i = 0;
//        for (FileInfo f : lst) {
//            fs[i++] = f;
//        }
//        return fs;
	}

	public FileInfo[] getFiles() {
		if (this.files == null) {
			return new FileInfo[0];
		} else {
			FileInfo fs[] = new FileInfo[files.size()];
			for (int i = 0; i < files.size(); i++) {
				fs[i] = files.get(i);
			}
			return fs;
		}
	}

	private int getRow(int r) {
		if (r == -1) {
			return -1;
		}
		return table.convertRowIndexToModel(r);
	}

	public void setItems(List<FileInfo> list) {
		this.files = list;
		applyFilter();
		// this.resizeColumnWidth(table);
//        if (showHiddenFiles) {
//            sortAndAddItems(list);
//        } else {
//            List<FileInfo> list2 = new ArrayList<>();
//            for (int i = 0; i < list.size(); i++) {
//                FileInfo info = list.get(i);
//                if (!info.isHidden()) {
//                    list2.add(info);
//                }
//            }
//            sortAndAddItems(list2);
//        }
	}

	public final void resizeColumnWidth(JTable table) {
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		final TableColumnModel columnModel = table.getColumnModel();
		for (int column = 0; column < table.getColumnCount(); column++) {
			// log.info("running..");
			TableColumn col = columnModel.getColumn(column);
//			col.getHeaderRenderer().getTableCellRendererComponent(table, col.getHeaderValue(),
//					false, false, 0, 0).getpre;
			if (column == 0) {
				col.setPreferredWidth(200);
			} else if (column == 3) {
				col.setPreferredWidth(120);
			} else {
				col.setPreferredWidth(100);
			}
		}
	}

	public void setFolderViewTransferHandler(DndTransferHandler transferHandler) {
//        this.list.setTransferHandler(transferHandler);
		this.table.setTransferHandler(transferHandler);
	}

//    public void sortView(int index, boolean asc) {
//        this.sortIndex = index;
//        this.sortAsc = asc;
//        log.info("Sort click- index: " + index + " asc: " + asc);
//        this.setItems(this.files);
//
////        List<FileInfo> fileInfoList = new ArrayList<>();
////        for (int i = 0; i < listModel.getSize(); i++) {
////            fileInfoList.add(listModel.get(i));
////        }
////        sortAndAddItems(fileInfoList);
//    }

//    private void sortAndAddItems(List<FileInfo> fileInfoList) {
//        switch (this.sortIndex) {
//            case 0:
//                fileInfoList.sort(new Comparator<FileInfo>() {
//                    @Override
//                    public int compare(FileInfo info1, FileInfo info2) {
//                        if (info1.getType() == FileType.Directory || info1.getType() == FileType.DirLink) {
//                            if (info2.getType() == FileType.Directory || info2.getType() == FileType.DirLink) {
//                                return info1.getName().compareToIgnoreCase(info2.getName());
//                            } else {
//                                return 1;
//                            }
//                        } else {
//                            if (info2.getType() == FileType.Directory || info2.getType() == FileType.DirLink) {
//                                return -1;
//                            } else {
//                                return info1.getName().compareToIgnoreCase(info2.getName());
//                            }
//                        }
//                    }
//                });
//                break;
//            case 1:
//                fileInfoList.sort(new Comparator<FileInfo>() {
//                    @Override
//                    public int compare(FileInfo o1, FileInfo o2) {
//                        Long s1 = o1.getSize();
//                        Long s2 = o2.getSize();
//                        return s1.compareTo(s2);
//                    }
//                });
//                break;
//            case 2:
//                fileInfoList.sort(new Comparator<FileInfo>() {
//                    @Override
//                    public int compare(FileInfo info1, FileInfo info2) {
//                        if (info1.getType() == FileType.Directory || info1.getType() == FileType.DirLink) {
//                            if (info2.getType() == FileType.Directory || info2.getType() == FileType.DirLink) {
//                                return info1.getLastModified().compareTo(info2.getLastModified());
//                            } else {
//                                return 1;
//                            }
//                        } else {
//                            if (info2.getType() == FileType.Directory || info2.getType() == FileType.DirLink) {
//                                return -1;
//                            } else {
//                                return info1.getLastModified().compareTo(info2.getLastModified());
//                            }
//                        }
//                    }
//                });
//                break;
//        }
//        if (!this.sortAsc) {
//            Collections.reverse(fileInfoList);
//        }
//        listModel.removeAllElements();
//        listModel.addAll(fileInfoList);
//    }

	public void setShowHiddenFiles(boolean showHiddenFiles) {
		this.showHiddenFiles = showHiddenFiles;
		applyFilter();
		// this.resizeColumnWidth(table);
	}

	private void applyFilter() {
//        int arr[] = new int[table.getColumnCount()];
//        final TableColumnModel columnModel = table.getColumnModel();
//        for (int column = 0; column < table.getColumnCount(); column++) {
//            TableColumn col = columnModel.getColumn(column);
//            arr[column] = col.getPreferredWidth();
//        }

		this.folderViewModel.clear();
		if (!this.showHiddenFiles) {
			List<FileInfo> list2 = new ArrayList<>();
			for (FileInfo info : this.files) {
				if (!info.getName().startsWith(".")) {
					list2.add(info);
				}
			}
			this.folderViewModel.addAll(list2);
		} else {
			this.folderViewModel.addAll(this.files);
		}

//        for (int column = 0; column < table.getColumnCount(); column++) {
//            TableColumn col = columnModel.getColumn(column);
//            col.setPreferredWidth(arr[column]);
//        }

		// table.setRowSorter(sorter);
	}

//    public int getSortIndex() {
//        return sortIndex;
//    }
//
//    public boolean isSortAsc() {
//        return sortAsc;
//    }

	public void sort(int index, SortOrder sortOrder) {
		sorter.setSortKeys(Arrays.asList(new RowSorter.SortKey(index, sortOrder)));
		sorter.sort();
	}

	public int getSortIndex() {
		for (SortKey sortKey : sorter.getSortKeys()) {
			return sortKey.getColumn();
		}
		return -1;
	}

	public boolean isSortAsc() {
		for (SortKey sortKey : sorter.getSortKeys()) {
			return sortKey.getSortOrder() == SortOrder.ASCENDING;
		}
		return false;
	}

	/**
	 * 
	 * Sets view mode: list or details view
	 * 
	 * Note: caller must call revalidate and repaint after calling this method
	 *
	 */
	public void refreshViewMode() {
		if (App.getGlobalSettings().isListViewEnabled()) {
			this.remove(tableScroller);
			this.add(listScroller);
		} else {
			this.remove(listScroller);
			this.add(tableScroller);
		}

		this.revalidate();
		this.repaint(0);
	}
}
