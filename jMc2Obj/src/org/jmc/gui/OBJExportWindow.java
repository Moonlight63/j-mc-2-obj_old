package org.jmc.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.ToolTipManager;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.jmc.CloudsExporter;
import org.jmc.ObjExporter;
import org.jmc.Options;
import org.jmc.ProgressCallback;
import org.jmc.StopCallback;
import org.jmc.TextureExporter;
import org.jmc.Options.OffsetType;
import org.jmc.util.Log;
import org.jmc.util.Messages;

@SuppressWarnings("serial")
public class OBJExportWindow extends JFrame implements ProgressCallback{

	private Preferences prefs;
	
	private boolean stop;
	
	private JPanel contentPane;
	
	private JRadioButton rdbtnNone;
	private JRadioButton rdbtnCenter;
	private JRadioButton rdbtnCustom;
	private JTextField txtX;
	private JTextField txtZ;
	
	private JCheckBox chckbxRenderWorldSides;
	private JCheckBox chckbxRenderBiomes;
	private JCheckBox chckbxRenderEntities;
	private JCheckBox chckbxRenderUnknownBlocks;
	private JCheckBox chckbxSeparateMat;
	private JCheckBox chckbxSeparateChunk;
	private JCheckBox chckbxSeparateBlock;
	private JCheckBox chckbxGeoOpt;
	private JCheckBox chckbxConvertOreTo;
	private JCheckBox chckbxMergeVerticies;
	private JCheckBox chckbxSingleMat;
	private JCheckBox chckbxSingleTexture;
	
	private JComboBox<String> cboxTexScale;
	private JCheckBox chckbxSeparateAlphaTexture;
	private JCheckBox chckbxCombineAllTextures;
	
	private JButton btnBrowseUV;
	private JTextField textFieldSingleTexUV;
	
	private JProgressBar progressBar;
	private JTextField textFieldMapScale;

	/**
	 * Create the frame.
	 */
	public OBJExportWindow() {
		setResizable(false);
		setBounds(100, 100, 525, 500);
		
		contentPane = new JPanel();
		
		prefs=MainWindow.settings.getPreferences();
		ToolTipManager.sharedInstance().setInitialDelay(0);
		
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(null);
		
		//ACTION HANDLERS
			DocumentListener tf_listener=new DocumentListener() {			
				@Override
				public void removeUpdate(DocumentEvent e) {
					saveSettings();
				}			
				@Override
				public void insertUpdate(DocumentEvent e) {
					saveSettings();
				}			
				@Override
				public void changedUpdate(DocumentEvent e) {
					saveSettings();
				}
			}; 
			
			AbstractAction genericSaveAction=new AbstractAction() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					saveSettings();
				}	
			};
			
			AbstractAction offsetSaveAction=new AbstractAction() {			
				@Override
				public void actionPerformed(ActionEvent ev) {
					if(ev.getSource() == rdbtnCustom)
					{
						txtX.setEnabled(true);
						txtZ.setEnabled(true);
					}
					else
					{
						txtX.setEnabled(false);
						txtZ.setEnabled(false);
					}
					saveSettings();
				}
			};
			
			AbstractAction exportTexFromMC = new AbstractAction() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					
					JFileChooser jfc=new JFileChooser(MainWindow.settings.getLastExportPath());
					jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					int retval=jfc.showDialog(OBJExportWindow.this, "Select Export Destination");
					if(retval!=JFileChooser.APPROVE_OPTION) return;
					ExportTextures(new File(jfc.getSelectedFile().toString().concat("/tex")), null, Double.parseDouble(cboxTexScale.getSelectedItem().toString().replace("x","")), chckbxCombineAllTextures.isSelected(), chckbxSeparateAlphaTexture.isSelected());
					
				}
			};
			
			AbstractAction exportTexFromRP = new AbstractAction() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					
					JFileChooser jfc=new JFileChooser(MainWindow.settings.getLastExportPath());
					jfc.setFileFilter(new FileNameExtensionFilter("Zip files", "zip", "ZIP", "Zip"));
					int retval=jfc.showDialog(OBJExportWindow.this, "Select Resource Pack");
					if(retval!=JFileChooser.APPROVE_OPTION) return;
					JFileChooser jfcDest=new JFileChooser(MainWindow.settings.getLastExportPath());
					jfcDest.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					retval=jfcDest.showDialog(OBJExportWindow.this, "Select Export Destination");
					if(retval!=JFileChooser.APPROVE_OPTION) return;
					ExportTextures(new File(jfcDest.getSelectedFile().toString().concat("/tex")), jfc.getSelectedFile(), Double.parseDouble(cboxTexScale.getSelectedItem().toString().replace("x","")), chckbxCombineAllTextures.isSelected(), chckbxSeparateAlphaTexture.isSelected());
					
				}
			};
			
			AbstractAction exportCloudsFromMC = new AbstractAction() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					
					JFileChooser jfc=new JFileChooser(MainWindow.settings.getLastExportPath()){
						 @Override
						    public void approveSelection(){
						        File f = getSelectedFile();
						        if(!f.toString().substring(f.toString().length()-4).contentEquals(".obj") || f.toString().length() < 4)
						        	setSelectedFile(new File(f.toString() + ".obj")); f = getSelectedFile();
						        
						        if(f.exists()){
						            int result = JOptionPane.showConfirmDialog(this,"The file exists, overwrite?","Existing file",JOptionPane.YES_NO_CANCEL_OPTION);
						            switch(result){
						                case JOptionPane.YES_OPTION:
						                    super.approveSelection();
						                    return;
						                case JOptionPane.NO_OPTION:
						                    return;
						                case JOptionPane.CLOSED_OPTION:
						                    return;
						                case JOptionPane.CANCEL_OPTION:
						                    cancelSelection();
						                    return;
						            }
						        }
						        super.approveSelection();
						    }  
					};
					jfc.setFileFilter(new FileNameExtensionFilter("Obj files", "obj", "OBJ", "Obj"));
					int retval=jfc.showDialog(OBJExportWindow.this, "Select Export Destination");
					if(retval!=JFileChooser.APPROVE_OPTION) return;
					ExportCloudsOBJ(new File(jfc.getCurrentDirectory().toString()), jfc.getSelectedFile(), null);
					
				}
			};
			
			AbstractAction exportCloudsFromRP = new AbstractAction() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					
					JFileChooser jfcRP=new JFileChooser(MainWindow.settings.getLastExportPath());
					jfcRP.setFileFilter(new FileNameExtensionFilter("Zip files", "zip", "ZIP", "Zip"));
					int retval=jfcRP.showDialog(OBJExportWindow.this, "Select Resource Pack");
					if(retval!=JFileChooser.APPROVE_OPTION) return;
					
					JFileChooser jfc=new JFileChooser(MainWindow.settings.getLastExportPath()){
						 @Override
						    public void approveSelection(){
						        File f = getSelectedFile();
						        if(!f.toString().substring(f.toString().length()-4).contentEquals(".obj") || f.toString().length() < 4)
						        	setSelectedFile(new File(f.toString() + ".obj")); f = getSelectedFile();
						        
						        if(f.exists()){
						            int result = JOptionPane.showConfirmDialog(this,"The file exists, overwrite?","Existing file",JOptionPane.YES_NO_CANCEL_OPTION);
						            switch(result){
						                case JOptionPane.YES_OPTION:
						                    super.approveSelection();
						                    return;
						                case JOptionPane.NO_OPTION:
						                    return;
						                case JOptionPane.CLOSED_OPTION:
						                    return;
						                case JOptionPane.CANCEL_OPTION:
						                    cancelSelection();
						                    return;
						            }
						        }
						        super.approveSelection();
						    }  
					};
					
					jfc.setFileFilter(new FileNameExtensionFilter("Obj files", "obj", "OBJ", "Obj"));
					retval=jfc.showDialog(OBJExportWindow.this, "Select Export Destination");
					if(retval!=JFileChooser.APPROVE_OPTION) return;
					ExportCloudsOBJ(new File(jfc.getCurrentDirectory().toString()), jfc.getSelectedFile(), jfcRP.getSelectedFile());
					
				}
			};
			
			AbstractAction uvSelect = new AbstractAction() {			
				@Override
				public void actionPerformed(ActionEvent arg0) {

					JFileChooser jfcFile=new JFileChooser(MainWindow.settings.getLastExportPath());
					jfcFile.setDialogTitle("UV File");
					jfcFile.setFileFilter(new FileNameExtensionFilter("UVfile", "uv"));
					if(jfcFile.showDialog(OBJExportWindow.this, "Select UV File")!=JFileChooser.APPROVE_OPTION)	{
						return;
					}

					File save_path=jfcFile.getSelectedFile();													
					textFieldSingleTexUV.setText(save_path.getAbsolutePath());
				}
			};
			
			
		//END ACTION HANDLERS
		
		//MAP OFFSET
			JPanel pMapExportOffset = new JPanel();
			pMapExportOffset.setBounds(5, 5, 200, 100);
			pMapExportOffset.setToolTipText("Map Offset Options");
			pMapExportOffset.setBorder(new TitledBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null), "Map Export Ofset", TitledBorder.CENTER, TitledBorder.TOP, null, new Color(0, 0, 0)));
			pMapExportOffset.setLayout(null);
					
			rdbtnNone = new JRadioButton("None");
			rdbtnNone.setBounds(6, 16, 70, 23);
			rdbtnNone.setSelected(true);
			pMapExportOffset.add(rdbtnNone);
			
			rdbtnCenter = new JRadioButton("Center");
			rdbtnCenter.setBounds(6, 42, 70, 23);
			pMapExportOffset.add(rdbtnCenter);
			
			rdbtnCustom = new JRadioButton("Custom");
			rdbtnCustom.setBounds(6, 68, 70, 23);
			pMapExportOffset.add(rdbtnCustom);
			
			ButtonGroup gOffset=new ButtonGroup();
			gOffset.add(rdbtnNone);
			gOffset.add(rdbtnCenter);
			gOffset.add(rdbtnCustom);
			
			//rdbtnNone.setActionCommand("None");
			//rdbtnCenter.setActionCommand("Center");
			//rdbtnCustom.setActionCommand("Custom");
			
			JLabel lblX = new JLabel("X:");
			lblX.setBounds(110, 33, 10, 14);
			pMapExportOffset.add(lblX);
			
			JLabel lblY = new JLabel("Y:");
			lblY.setBounds(110, 63, 10, 14);
			pMapExportOffset.add(lblY);
			
			txtX = new JTextField();
			txtX.setText("0");
			txtX.setBounds(122, 30, 68, 20);
			txtX.setColumns(10);
			pMapExportOffset.add(txtX);
			
			txtZ = new JTextField();
			txtZ.setText("0");
			txtZ.setBounds(122, 60, 68, 20);
			txtZ.setColumns(10);
			pMapExportOffset.add(txtZ);
			
			if(!rdbtnCustom.isSelected()){
				txtX.setEnabled(false);
				txtZ.setEnabled(false);
			}
		//END MAP OFFSET
		
		
		//TEXTURE EXPORT OPTIONS
			JPanel pTextureOptions = new JPanel();
			pTextureOptions.setBorder(new TitledBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null), "Texture Options", TitledBorder.CENTER, TitledBorder.TOP, null, null));
			pTextureOptions.setBounds(5, 116, 200, 318);
			pTextureOptions.setLayout(null);
			
			//PRESCALE
				JLabel lblPrescaleTextures = new JLabel("Pre-Scale Textures");
				lblPrescaleTextures.setBounds(25, 24, 91, 14);
				pTextureOptions.add(lblPrescaleTextures);
				
				cboxTexScale = new JComboBox<String>();
				cboxTexScale.setBounds(126, 21, 47, 20);
				cboxTexScale.setMaximumRowCount(16);
				cboxTexScale.setModel(new DefaultComboBoxModel<String>(new String[] {"1x", "2x", "4x", "8x", "16x"}));
				pTextureOptions.add(cboxTexScale);
			//END PRESCALE
			
			chckbxSeparateAlphaTexture = new JCheckBox("Separate Alpha Texture");
			chckbxSeparateAlphaTexture.setBounds(25, 45, 148, 23);
			pTextureOptions.add(chckbxSeparateAlphaTexture);
			
			chckbxCombineAllTextures = new JCheckBox("Combine All Textures");
			chckbxCombineAllTextures.setBounds(25, 71, 148, 23);
			pTextureOptions.add(chckbxCombineAllTextures);
			
			JLabel lblExportTexturesFrom = new JLabel("Export Textures From:");
			lblExportTexturesFrom.setBounds(25, 107, 144, 14);
			pTextureOptions.add(lblExportTexturesFrom);
			
			JButton btnMinecraftDefault = new JButton("Minecraft Default");
			btnMinecraftDefault.setBounds(25, 126, 148, 23);
			pTextureOptions.add(btnMinecraftDefault);
			
			JButton btnCustomResourcePack = new JButton("Custom Resource Pack");
			btnCustomResourcePack.setBounds(25, 155, 148, 23);
			pTextureOptions.add(btnCustomResourcePack);
		
			//CLOUD EXPORT
				JPanel pCloudExport = new JPanel();
				pCloudExport.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
				pCloudExport.setBounds(25, 214, 148, 85);
				pCloudExport.setLayout(null);
				pTextureOptions.add(pCloudExport);
		
				JLabel lblExportCloudsObj = new JLabel("Export Clouds OBJ:");
				lblExportCloudsObj.setBounds(27, 7, 93, 14);
				pCloudExport.add(lblExportCloudsObj);
				
				JButton btnMinecraftTextures = new JButton("From Minecraft");
				btnMinecraftTextures.setBounds(9, 26, 129, 23);
				pCloudExport.add(btnMinecraftTextures);
				
				JButton btnFromResourcePack = new JButton("From Resource Pack");
				btnFromResourcePack.setBounds(9, 54, 129, 23);
				pCloudExport.add(btnFromResourcePack);
			//END CLOUD EXPORT
		//END TEXTURE EXPORT
		
		//MISC EXPORT OPTIONS
			JPanel pExportOptions = new JPanel();
			pExportOptions.setBorder(new TitledBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null), "Map Export Options", TitledBorder.CENTER, TitledBorder.TOP, null, null));
			pExportOptions.setBounds(215, 5, 300, 429);
			pExportOptions.setLayout(null);
			
			
			//MAP SCALE
				JLabel lblMapScale = new JLabel("Map Scale");
				lblMapScale.setBounds(22, 19, 67, 14);
				pExportOptions.add(lblMapScale);
				
				textFieldMapScale = new JTextField();
				textFieldMapScale.setBounds(82, 16, 86, 20);
				textFieldMapScale.setColumns(10);
				pExportOptions.add(textFieldMapScale);
				
			//END MAP SCALE
			
			chckbxRenderWorldSides = new JCheckBox("Render World Sides & Bottom");
			chckbxRenderWorldSides.setBounds(16, 37, 167, 23);
			pExportOptions.add(chckbxRenderWorldSides);
			
			chckbxRenderBiomes = new JCheckBox("Render Biomes");
			chckbxRenderBiomes.setBounds(16, 63, 167, 23);
			pExportOptions.add(chckbxRenderBiomes);
			
			chckbxRenderEntities = new JCheckBox("Render Entities (paintings)");
			chckbxRenderEntities.setBounds(16, 89, 167, 23);
			pExportOptions.add(chckbxRenderEntities);
			
			chckbxRenderUnknownBlocks = new JCheckBox("Render Unknown Blocks");
			chckbxRenderUnknownBlocks.setBounds(16, 115, 167, 23);
			pExportOptions.add(chckbxRenderUnknownBlocks);
			
			chckbxSeparateMat = new JCheckBox("Separate Object per Material");
			chckbxSeparateMat.setBounds(16, 141, 167, 23);
			pExportOptions.add(chckbxSeparateMat);
			
			chckbxSeparateChunk = new JCheckBox("Separate Object per Chunk");
			chckbxSeparateChunk.setBounds(16, 167, 157, 23);
			pExportOptions.add(chckbxSeparateChunk);
			
			//Separate obj per block
				chckbxSeparateBlock = new JCheckBox("Separate Object per Block");
				chckbxSeparateBlock.setBounds(16, 193, 157, 23);
				pExportOptions.add(chckbxSeparateBlock);
				
				JLabel lblSepBlockWarn = new JLabel("!!!");
				lblSepBlockWarn.setFont(new Font("Tahoma", Font.BOLD, 11));
				lblSepBlockWarn.setToolTipText("WARNING! Will increase model size DRASTICALLY");
				lblSepBlockWarn.setForeground(Color.RED);
				lblSepBlockWarn.setBounds(179, 195, 9, 19);
				pExportOptions.add(lblSepBlockWarn);
			//End
				
			chckbxGeoOpt = new JCheckBox("Optimize Mesh");
			chckbxGeoOpt.setBounds(16, 219, 157, 23);
			pExportOptions.add(chckbxGeoOpt);
			
			JButton btnBlocksToExport = new JButton("Select Blocks to Export");
			btnBlocksToExport.setBounds(19, 242, 164, 23);
			pExportOptions.add(btnBlocksToExport);
				
			chckbxConvertOreTo = new JCheckBox("Convert Ore to Stone");
			chckbxConvertOreTo.setBounds(16, 266, 157, 23);
			pExportOptions.add(chckbxConvertOreTo);
			
			chckbxMergeVerticies = new JCheckBox("Merge Verticies");
			chckbxMergeVerticies.setBounds(16, 292, 157, 23);
			pExportOptions.add(chckbxMergeVerticies);
			
			//ONE MATERIAL
				chckbxSingleMat = new JCheckBox("Only Create 1 Material");
				chckbxSingleMat.setBounds(16, 318, 133, 23);
				pExportOptions.add(chckbxSingleMat);
				
				JLabel lblOneMatHelp = new JLabel("???");
				lblOneMatHelp.setToolTipText("Should be used with the Single Texture Option below");
				lblOneMatHelp.setForeground(Color.RED);
				lblOneMatHelp.setFont(new Font("Tahoma", Font.BOLD, 11));
				lblOneMatHelp.setBounds(155, 322, 46, 14);
				pExportOptions.add(lblOneMatHelp);
			//END ONE MAT
			
			//Use single tex
				chckbxSingleTexture = new JCheckBox("Use Single Texture");
				chckbxSingleTexture.setBounds(16, 344, 115, 23);
				pExportOptions.add(chckbxSingleTexture);
				
				JLabel lblSingleTexHelp = new JLabel("???");
				lblSingleTexHelp.setToolTipText("Use the Combine Textures option in the Texture Export Option to generate a uv file");
				lblSingleTexHelp.setFont(new Font("Tahoma", Font.BOLD, 11));
				lblSingleTexHelp.setForeground(Color.RED);
				lblSingleTexHelp.setBounds(137, 348, 46, 14);
				pExportOptions.add(lblSingleTexHelp);
				
				textFieldSingleTexUV = new JTextField();
				textFieldSingleTexUV.setBounds(20, 367, 191, 21);
				textFieldSingleTexUV.setColumns(10);
				pExportOptions.add(textFieldSingleTexUV);
				
				btnBrowseUV = new JButton("Browse");
				btnBrowseUV.setBounds(221, 366, 67, 23);
				pExportOptions.add(btnBrowseUV);
			//End
						
			//EXPORT
				final JButton btnStartExport = new JButton("Start Export");
				btnStartExport.setBounds(19, 395, 91, 23);
				pExportOptions.add(btnStartExport);
				
				final JButton btnForceStop = new JButton("Force Stop");
				btnForceStop.setEnabled(false);
				btnForceStop.setBounds(120, 395, 91, 23);
				pExportOptions.add(btnForceStop);
			//END EXPORT
			
		//END MISC OPTIONS
				
		loadSettings();
			
		progressBar = new JProgressBar();
		progressBar.setBounds(5, 445, 510, 25);
		
		rdbtnNone.addActionListener(offsetSaveAction);
		rdbtnCenter.addActionListener(offsetSaveAction);
		rdbtnCustom.addActionListener(offsetSaveAction);

		txtX.getDocument().addDocumentListener(tf_listener);
		txtZ.getDocument().addDocumentListener(tf_listener);
		textFieldMapScale.getDocument().addDocumentListener(tf_listener);
		textFieldSingleTexUV.getDocument().addDocumentListener(tf_listener);
		
		btnMinecraftDefault.addActionListener(exportTexFromMC);
		btnCustomResourcePack.addActionListener(exportTexFromRP);
		btnMinecraftTextures.addActionListener(exportCloudsFromMC);
		btnFromResourcePack.addActionListener(exportCloudsFromRP);
		btnBrowseUV.addActionListener(uvSelect);
		btnBlocksToExport.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				MainWindow.blocksWindow.setVisible(true);
			}
		});
		
		btnStartExport.addActionListener(new AbstractAction() {

			@Override
			public void actionPerformed(ActionEvent e)
			{
				
				JFileChooser jfc=new JFileChooser(MainWindow.settings.getLastExportPath()){
					@Override
				    public void approveSelection(){
				        File f = getSelectedFile();
				        if(!f.toString().substring(f.toString().length()-4).contentEquals(".obj") || f.toString().length() < 4)
				        	setSelectedFile(new File(f.toString() + ".obj")); f = getSelectedFile();	
				        File f2 = new File(f.toString().replace(".obj", ".mtl"));
				        
				        if(f.exists()){
				            int result = JOptionPane.showConfirmDialog(this,"The OBJ file exists, overwrite?","Existing file",JOptionPane.YES_NO_CANCEL_OPTION);
				            switch(result){
				                case JOptionPane.YES_OPTION:
				                	
				                    if(f2.exists()){
							            int result2 = JOptionPane.showConfirmDialog(this,"The MTL file exists, overwrite?","Existing file",JOptionPane.YES_NO_CANCEL_OPTION);
							            switch(result2){
							                case JOptionPane.YES_OPTION:
							                	sendExport();
							                    super.approveSelection();
							                    return;
							                case JOptionPane.NO_OPTION:
							                    return;
							                case JOptionPane.CLOSED_OPTION:
							                    return;
							                case JOptionPane.CANCEL_OPTION:
							                    cancelSelection();
							                    return;
							            }
							        }
							        else{
							        	sendExport();
							        	super.approveSelection();
							        	return;
							        }
				                    
				                case JOptionPane.NO_OPTION:
				                    return;
				                case JOptionPane.CLOSED_OPTION:
				                    return;
				                case JOptionPane.CANCEL_OPTION:
				                    cancelSelection();
				                    return;
				            }
				        }
				        else{
				            sendExport();
				        	super.approveSelection();
				        }
				        
				    }
					
					private void sendExport(){
						
						File savePath = getCurrentDirectory();
						Options.outputDir = savePath;
						Options.objFileName = getSelectedFile().getName();
						Options.mtlFileName = getSelectedFile().getName().replace(".obj", ".mtl");
						
						MainWindow.settings.setLastExportPath(savePath.toString());
						MainWindow.updateSelectionOptions();
						btnStartExport.setEnabled(false);
						btnForceStop.setEnabled(true);
						
						Thread t = new Thread(new Runnable() {
							@Override
							public void run() {
								stop=false;
		
								ObjExporter.export(OBJExportWindow.this,
									new StopCallback() {
										@Override
										public boolean stopRequested() {
											return stop;
										}
									}, 
									true,
									true);
								
								btnStartExport.setEnabled(true);
								btnForceStop.setEnabled(false);
							}
						});
						t.start();
						
				    }
					
				};
				
				jfc.showDialog(OBJExportWindow.this, "Select Export Destination");
			}
			
			
		});
		
		btnForceStop.addActionListener(new AbstractAction() {			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				stop = true;				
			}
		});
		
		cboxTexScale.addActionListener(genericSaveAction);
		chckbxSeparateAlphaTexture.addActionListener(genericSaveAction);
		chckbxCombineAllTextures.addActionListener(genericSaveAction);

		chckbxRenderWorldSides.addActionListener(genericSaveAction);
		chckbxRenderBiomes.addActionListener(genericSaveAction);
		chckbxRenderEntities.addActionListener(genericSaveAction);
		chckbxRenderUnknownBlocks.addActionListener(genericSaveAction);
		chckbxSeparateMat.addActionListener(genericSaveAction);
		chckbxSeparateChunk.addActionListener(genericSaveAction);
		chckbxSeparateBlock.addActionListener(genericSaveAction);
		chckbxGeoOpt.addActionListener(genericSaveAction);
		chckbxConvertOreTo.addActionListener(genericSaveAction);
		chckbxMergeVerticies.addActionListener(genericSaveAction);
		chckbxSingleMat.addActionListener(genericSaveAction);
		chckbxSingleTexture.addActionListener(genericSaveAction);

		contentPane.add(pMapExportOffset);
		contentPane.add(pTextureOptions);
		contentPane.add(pExportOptions);
		contentPane.add(progressBar);
		
		setContentPane(contentPane);
		
	}
	
	
	private void loadSettings(){
		
		textFieldMapScale.setText("" + prefs.getFloat("DEFAULT_SCALE", 1.0f));
		
		switch(prefs.getInt("OFFSET_TYPE", 0)){
		case 0:
			rdbtnNone.setSelected(true);
			txtX.setEnabled(false);
			txtZ.setEnabled(false);
			break;
		case 1:
			rdbtnCenter.setSelected(true);
			txtX.setEnabled(false);
			txtZ.setEnabled(false);
			break;
		case 2:
			rdbtnCustom.setSelected(true);
			txtX.setEnabled(true);
			txtZ.setEnabled(true);
			break;
		}
		
		txtX.setText(""+prefs.getInt("OFFSET_X", 0));
		txtZ.setText(""+prefs.getInt("OFFSET_Z", 0));
		
		chckbxRenderWorldSides.setSelected(prefs.getBoolean("RENDER_SIDES", true));
		chckbxRenderBiomes.setSelected(prefs.getBoolean("RENDER_BIOMES", true));
		chckbxRenderEntities.setSelected(prefs.getBoolean("RENDER_ENTITIES", true));
		chckbxRenderUnknownBlocks.setSelected(prefs.getBoolean("RENDER_UNKNOWN", true));
		chckbxSeparateMat.setSelected(prefs.getBoolean("OBJ_PER_MTL", true));
		chckbxSeparateChunk.setSelected(prefs.getBoolean("OBJ_PER_CHUNK", true));
		chckbxSeparateBlock.setSelected(prefs.getBoolean("OBJ_PER_BLOCK", true));
		chckbxGeoOpt.setSelected(prefs.getBoolean("OPTIMISE_GEO", false));
		chckbxConvertOreTo.setSelected(prefs.getBoolean("CONVERT_ORES", true));
		chckbxSingleMat.setSelected(prefs.getBoolean("SINGLE_MTL", true));
		chckbxMergeVerticies.setSelected(prefs.getBoolean("REMOVE_DUPLICATES", true));
		chckbxSingleTexture.setSelected(prefs.getBoolean("USE_UV_FILE", true));
		textFieldSingleTexUV.setText(prefs.get("UV_FILE", ""));

		cboxTexScale.setSelectedItem(""+prefs.getDouble("TEXTURE_SCALE_ID", 1.0));
		chckbxSeparateAlphaTexture.setSelected(prefs.getBoolean("TEXTURE_ALPHA", false));
		chckbxCombineAllTextures.setSelected(prefs.getBoolean("TEXTURE_MERGE", false));
		
		if(!chckbxSingleTexture.isSelected()){
			textFieldSingleTexUV.setEnabled(false); btnBrowseUV.setEnabled(false);
		}
		else{
			textFieldSingleTexUV.setEnabled(true); btnBrowseUV.setEnabled(true);
		}
		
	}
	
	private void saveSettings(){
		
		MainWindow.log("Saving Options");
		
		updateOptions();
		
		prefs.putFloat("DEFAULT_SCALE", Options.scale);
		prefs.putInt("OFFSET_X", Options.offsetX);
		prefs.putInt("OFFSET_Z", Options.offsetZ);
		
		switch(Options.offsetType)
		{
		case NONE:
			prefs.putInt("OFFSET_TYPE", 0);
			break;
		case CENTER:
			prefs.putInt("OFFSET_TYPE", 1);
			break;
		case CUSTOM:
			prefs.putInt("OFFSET_TYPE", 2);
			break;
		}

		switch(Options.objOverwriteAction)
		{
		case ASK:
			prefs.putInt("OBJ_OVERWRITE", 0);
			break;
		case ALWAYS:
			prefs.putInt("OBJ_OVERWRITE", 1);
			break;
		case NEVER:
			prefs.putInt("OBJ_OVERWRITE", 2);
			break;
		}

		switch(Options.mtlOverwriteAction)
		{
		case ASK:
			prefs.putInt("MTL_OVERWRITE", 0);
			break;
		case ALWAYS:
			prefs.putInt("MTL_OVERWRITE", 1);
			break;
		case NEVER:
			prefs.putInt("MTL_OVERWRITE", 2);
			break;
		}
		
		if(!chckbxSingleTexture.isSelected()){
			textFieldSingleTexUV.setEnabled(false); btnBrowseUV.setEnabled(false);
		}
		else{
			textFieldSingleTexUV.setEnabled(true); btnBrowseUV.setEnabled(true);
		}

		prefs.putBoolean("RENDER_SIDES", Options.renderSides);
		prefs.putBoolean("RENDER_BIOMES", Options.renderBiomes);
		prefs.putBoolean("RENDER_ENTITIES", Options.renderEntities);
		prefs.putBoolean("RENDER_UNKNOWN", Options.renderUnknown);
		prefs.putBoolean("OBJ_PER_MTL", Options.objectPerMaterial);
		prefs.putBoolean("OBJ_PER_CHUNK", Options.objectPerChunk);
		prefs.putBoolean("OBJ_PER_BLOCK", Options.objectPerBlock);
		prefs.putBoolean("OPTIMISE_GEO", Options.optimiseGeometry);
		prefs.putBoolean("CONVERT_ORES", Options.convertOres);
		prefs.putBoolean("SINGLE_MTL", Options.singleMaterial);
		prefs.putBoolean("REMOVE_DUPLICATES", Options.removeDuplicates);
		prefs.putBoolean("USE_UV_FILE", Options.useUVFile);
		prefs.put("UV_FILE", Options.UVFile.getAbsolutePath());
		
		prefs.putDouble("TEXTURE_SCALE_ID", Options.textureScale);
		prefs.putBoolean("TEXTURE_ALPHA", Options.textureAlpha);
		prefs.putBoolean("TEXTURE_MERGE", Options.textureMerge);
		
		MainWindow.log("" + prefs.getBoolean("RENDER_SIDES", false));
		
	}
	
	private void updateOptions(){
				
		try{
			Options.scale = Float.parseFloat(textFieldMapScale.getText());
		}catch (NumberFormatException e) {
			JOptionPane.showMessageDialog(this, Messages.getString("OBJExportOptions.SCALE_NUM_ERR"));
			Options.scale =  1.0f;
		}
		
		try{
			String txt=txtX.getText();
			if(!txt.isEmpty() && !txt.equals("-"))
				Options.offsetX = Integer.parseInt(txt);
			txt=txtZ.getText();
			if(!txt.isEmpty() && !txt.equals("-"))
				Options.offsetZ = Integer.parseInt(txt);

		}catch (NumberFormatException e) {
			Log.error("Offset number format error!", e, false);
		}
		
		if(rdbtnCenter.isSelected())
			Options.offsetType = OffsetType.CENTER;
		else if(rdbtnCustom.isSelected())
			Options.offsetType = OffsetType.CUSTOM;
		else
			Options.offsetType = OffsetType.NONE;
		
		
		Options.renderSides = chckbxRenderWorldSides.isSelected();
		Options.renderBiomes = chckbxRenderBiomes.isSelected();
		Options.renderEntities = chckbxRenderEntities.isSelected();
		Options.renderUnknown = chckbxRenderUnknownBlocks.isSelected();
		Options.objectPerMaterial = chckbxSeparateMat.isSelected();
		Options.objectPerChunk = chckbxSeparateChunk.isSelected();
		Options.objectPerBlock = chckbxSeparateBlock.isSelected();
		Options.optimiseGeometry = chckbxGeoOpt.isSelected();
		Options.convertOres = chckbxConvertOreTo.isSelected();
		Options.singleMaterial = chckbxSingleMat.isSelected();
		Options.removeDuplicates = chckbxMergeVerticies.isSelected();
		Options.useUVFile=chckbxSingleTexture.isSelected();
		Options.UVFile=new File(textFieldSingleTexUV.getText());
		
		String txt=cboxTexScale.getSelectedItem().toString();
		if(!txt.isEmpty())
		{
			if(txt.endsWith("x")) txt=txt.substring(0,txt.length()-1);
			
			try{
				Options.textureScale=Double.parseDouble(txt);
			}catch (NumberFormatException e) {
				Log.error(Messages.getString("TexsplitDialog.ERR_SCALE"), e,false);
			}
		}
		
		Options.textureAlpha=chckbxSeparateAlphaTexture.isSelected();
		Options.textureMerge=chckbxCombineAllTextures.isSelected();
		
	}
	
	private void ExportTextures(final File destination, final File texturepack, final double texScale, final boolean texMerge, final boolean alphas){
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					if(texMerge){
						TextureExporter.mergeTextures(destination, texturepack, texScale, alphas,	OBJExportWindow.this);
						OBJExportWindow.this.textFieldSingleTexUV.setText(new File(destination, "texture.uv").toString());
					}
					else{
						TextureExporter.splitTextures(destination, texturepack, texScale, alphas,	OBJExportWindow.this);
					}
				}
				catch (Exception e) {
					Log.error(Messages.getString("TexsplitDialog.ERR_EXP"), e);
				}			
			}
		}).start();								
	}
	
	private void ExportCloudsOBJ(final File destination, final File file, final File texturepack){
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					CloudsExporter.exportClouds(destination, texturepack, file.getName());
				}
				catch (Exception e) {
					Log.error(Messages.getString("TexsplitDialog.ERR_EXP"), e);
				}			
			}
		}).start();								
	}
	
	@Override
	public void setProgress(float value) {
		progressBar.setValue((int)(value*100f));		
	}
	
}